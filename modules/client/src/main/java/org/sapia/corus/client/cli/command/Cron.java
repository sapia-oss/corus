package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.cli.command.cron.CronWizard;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.ubik.util.Collects;

/**
 * @author Yanick Duchesne
 */
public class Cron extends CorusCliCommand {

  private TableDef CRON_TBL = TableDef.newInstance()
      .createCol("id", 17).createCol("dist", 7).createCol("version", 7).createCol("vm", 7)
      .createCol("profile", 10).createCol("hour", 2).createCol("minute", 2)
      .createCol("day", 2).createCol("month", 2).createCol("year", 4);

  private TableDef HOST_TBL = TableDef.newInstance()
      .createCol("val", 78);

  // --------------------------------------------------------------------------

  private static final String STAR   = "*";
  private static final String ADD    = "add";
  private static final String LIST   = "list";
  private static final String LS     = "ls";
  private static final String REMOVE = "delete";
  
  private static final OptionDef JOB_ID = new OptionDef("i", true);
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      JOB_ID, OPT_DIST, OPT_VERSION, OPT_PROFILE, OPT_PROCESS_NAME, OPT_CLUSTER
  );

  // --------------------------------------------------------------------------

  private static final String[] DAYS = new String[] { "Su", "Mo", "Tu", "We", "Th", "Fr", "Sa" };

  // --------------------------------------------------------------------------
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
    CRON_TBL.setTableWidth(context.getConsole().getWidth());
    HOST_TBL.setTableWidth(context.getConsole().getWidth());
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    CmdLine cmd = ctx.getCommandLine();
    CronWizard wiz = new CronWizard();

    if (cmd.hasNext() && cmd.isNextArg()) {
      Arg arg = (Arg) cmd.next();

      if (arg.getName().equals(LIST) || arg.getName().equals(LS)) {
        displayResults(ctx.getCorus().getCronFacade().getCronJobs(getClusterInfo(ctx)), ctx);
      } else if (arg.getName().equals(REMOVE)) {
        ctx.getCorus().getCronFacade().removeCronJob(cmd.assertOption(JOB_ID.getName(), true).getValue());
      } else if (arg.getName().equals(ADD)) {
        wiz.execute(cmd, ctx);
      } else {
        throw new InputException("Unrecognized argument: " + arg.getName() + "; should be: add | list | delete ");
      }
    } else {
      throw new InputException("Missing argument; should be: add | list | delete ");
    }
  }
  
  private void displayResults(Results<List<CronJobInfo>> res, CliContext ctx) {

    while (res.hasNext()) {
      Result<List<CronJobInfo>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for (CronJobInfo job : result.getData()) {
        displayJob(job, ctx);
      }
    }
  }

  private void displayJob(CronJobInfo info, CliContext ctx) {
    Table cronTable = CRON_TBL.createTable(ctx.getConsole().out());

    cronTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = cronTable.newRow();
    row.getCellAt(CRON_TBL.col("id").index()).append(info.getId());
    row.getCellAt(CRON_TBL.col("dist").index()).append(info.getDistribution());
    row.getCellAt(CRON_TBL.col("version").index()).append(info.getVersion());
    row.getCellAt(CRON_TBL.col("vm").index()).append(info.getProcessName());
    row.getCellAt(CRON_TBL.col("profile").index()).append(info.getProfile());
    row.getCellAt(CRON_TBL.col("hour").index()).append(doAppend(info.getHour()));
    row.getCellAt(CRON_TBL.col("minute").index()).append(doAppend(info.getMinute()));

    if (info.getDayOfWeek() == CronJobInfo.UNDEFINED) {
      row.getCellAt(CRON_TBL.col("day").index()).append(STAR);
    } else {
      row.getCellAt(CRON_TBL.col("day").index()).append(DAYS[info.getDayOfWeek() - 1]);
    }

    row.getCellAt(CRON_TBL.col("month").index()).append(doAppend(info.getMonth()));
    row.getCellAt(CRON_TBL.col("year").index()).append(doAppend(info.getYear()));
    row.flush();
  }

  private static String doAppend(int value) {
    if (value == CronJobInfo.UNDEFINED) {
      return STAR;
    } else {
      String toReturn = Integer.toString(value);

      if (toReturn.length() < 2) {
        return "0" + toReturn;
      } else {
        return toReturn;
      }
    }
  }

  private void displayHeader(CorusHost addr, CliContext ctx) {
    Table hostTable = HOST_TBL.createTable(ctx.getConsole().out());
    Table infoTable = CRON_TBL.createTable(ctx.getConsole().out());

    hostTable.drawLine('=', 0, ctx.getConsole().getWidth());
    Row row = hostTable.newRow();
    row.getCellAt(HOST_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    hostTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = infoTable.newRow();
    headers.getCellAt(CRON_TBL.col("id").index()).append("ID");
    headers.getCellAt(CRON_TBL.col("dist").index()).append("Dist.");
    headers.getCellAt(CRON_TBL.col("version").index()).append("Version");
    headers.getCellAt(CRON_TBL.col("vm").index()).append("Process Name");
    headers.getCellAt(CRON_TBL.col("profile").index()).append("Profile");
    headers.getCellAt(CRON_TBL.col("hour").index()).append("hh");
    headers.getCellAt(CRON_TBL.col("minute").index()).append("mm");
    headers.getCellAt(CRON_TBL.col("day").index()).append("DD");
    headers.getCellAt(CRON_TBL.col("month").index()).append("MM");
    headers.getCellAt(CRON_TBL.col("year").index()).append("YYYY");
    headers.flush();
  }
}
