package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.ubik.util.Collects;

/**
 * Displays process status.
 * 
 * @author Yanick Duchesne
 */
public class Status extends CorusCliCommand {

  private static final TableDef STAT_TBL = TableDef.newInstance().createCol("pid", 15).createCol("context", 15).createCol("name", 15)
      .createCol("value", 25);

  private static final TableDef TITLE_TBL = TableDef.newInstance().createCol("val", 78);

  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_DIST, OPT_VERSION, OPT_PROCESS_NAME, OPT_PROFILE, OPT_PROCESS_ID, OPT_CLUSTER
  );
  

  // --------------------------------------------------------------------------

  @Override
  protected List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    String dist = null;
    String version = null;
    String profile = null;
    String vmName = null;
    String pid = null;

    CmdLine cmd = ctx.getCommandLine();

    if (cmd.containsOption(OPT_DIST.getName(), true)) {
      dist = cmd.assertOption(OPT_DIST.getName(), true).getValue();
    }

    if (cmd.containsOption(OPT_VERSION.getName(), true)) {
      version = cmd.assertOption(OPT_VERSION.getName(), true).getValue();
    }

    if (cmd.containsOption(OPT_PROFILE.getName(), true)) {
      profile = cmd.assertOption(OPT_PROFILE.getName(), true).getValue();
    }

    if (cmd.containsOption(OPT_PROCESS_NAME.getName(), true)) {
      vmName = cmd.assertOption(OPT_PROCESS_NAME.getName(), true).getValue();
    }

    if (cmd.containsOption(OPT_PROCESS_ID.getName(), true)) {
      pid = cmd.assertOption(OPT_PROCESS_ID.getName(), true).getValue();
    }

    ClusterInfo cluster = getClusterInfo(ctx);

    Results<List<ProcStatus>> res;

    if (pid != null) {
      try {
        ProcStatus stat = ctx.getCorus().getProcessorFacade().getStatusFor(pid);
        displayHeader(ctx.getCorus().getContext().getServerHost(), ctx);
        displayStatus(stat, ctx);
      } catch (ProcessNotFoundException e) {
        throw new InputException(e.getMessage());
      }
    } else {
      ProcessCriteria criteria = ProcessCriteria.builder().name(vmName).distribution(dist).version(version).profile(profile).build();
      res = ctx.getCorus().getProcessorFacade().getStatus(criteria, cluster);
      Sorting.sortList(res, ProcStatus.class, ctx.getSortSwitches());
      displayResults(res, ctx);
    }
  }

  private void displayResults(Results<List<ProcStatus>> res, CliContext ctx) {

    while (res.hasNext()) {
      Result<List<ProcStatus>> result = res.next();
      displayHeader(result.getOrigin(), ctx);

      for (ProcStatus stat : result.getData()) {
        displayStatus(stat, ctx);
      }
    }
  }

  private void displayStatus(ProcStatus stat, CliContext ctx) {
    Table procTable = STAT_TBL.createTable(ctx.getConsole().out());

    procTable.drawLine('-', 0, CONSOLE_WIDTH);

    Row row = procTable.newRow();
    row.getCellAt(STAT_TBL.col("pid").index()).append(stat.getProcessID());
    List<Context> contexts = stat.getContexts();
    if (contexts.size() == 0) {
      row.flush();
      return;
    }
    Context context;
    for (int i = 0; i < contexts.size(); i++) {
      context = (Context) contexts.get(i);
      row.getCellAt(STAT_TBL.col("context").index()).append(context.getName());
      List<Param> params = context.getParams();
      if (params.size() == 0) {
        row.flush();
        row = procTable.newRow();
        continue;
      }
      Param param;
      for (int j = 0; j < params.size(); j++) {
        param = (Param) params.get(j);
        row.getCellAt(STAT_TBL.col("name").index()).append(param.getName());
        row.getCellAt(STAT_TBL.col("value").index()).append(param.getValue());
        row.flush();
        if (j < params.size() - 1) {
          row = procTable.newRow();
        }
      }
      if (i < contexts.size() - 1) {
        row = procTable.newRow();
      }
    }

  }

  private void displayHeader(CorusHost addr, CliContext ctx) {
    Table procTable = STAT_TBL.createTable(ctx.getConsole().out());
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());

    procTable.drawLine('=', 0, CONSOLE_WIDTH);

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    procTable.drawLine(' ', 0, CONSOLE_WIDTH);

    Row headers = procTable.newRow();

    headers.getCellAt(STAT_TBL.col("pid").index()).append("PID");
    headers.getCellAt(STAT_TBL.col("context").index()).append("Context");
    headers.getCellAt(STAT_TBL.col("name").index()).append("Name");
    headers.getCellAt(STAT_TBL.col("value").index()).append("Value");
    headers.flush();
  }
}
