package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.processor.PortCriteria;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.util.Collects;

/**
 * Displays process info.
 *
 * @author Yanick Duchesne
 */
public class Ps extends CorusCliCommand {

  private static final int OS_PID_LEN = 10;
  
  private final TableDef PROC_TBL = TableDef.newInstance()
      .createCol("dist", 15).createCol("version", 7).createCol("profile", 8)
      .createCol("name", 11).createCol("pid", 14).createCol("ospid", 6).createCol("status", 9);

  private final TableDef PROC_PORTS_TBL = TableDef.newInstance()
      .createCol("dist", 15).createCol("version", 7).createCol("profile", 8)
      .createCol("name", 11).createCol("pid", 14).createCol("ports", 15);

  private final TableDef PROC_NUMA_TBL = TableDef.newInstance()
      .createCol("dist", 15).createCol("version", 7).createCol("profile", 8)
      .createCol("name", 11).createCol("pid", 14).createCol("numanode", 15);

  private TableDef TITLE_TBL = TableDef.newInstance()
      .createCol("val", 78);

  // --------------------------------------------------------------------------

  private static final OptionDef OPT_PORTS = new OptionDef("ports", false);
  private static final OptionDef OPT_NUMA  = new OptionDef("numa", false);
  private static final OptionDef OPT_CLEAN = new OptionDef("clean", false);

  protected static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_PROCESS_ID, OPT_PROCESS_NAME, OPT_DIST, OPT_VERSION, OPT_PROFILE,
      OPT_PORTS, OPT_NUMA, OPT_CLEAN, OPT_PORT_RANGE, OPT_CLUSTER
  );

  // --------------------------------------------------------------------------

  @Override
  public java.util.List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  @Override
  protected void doInit(CliContext context) {
    PROC_TBL.setTableWidth(context.getConsole().getWidth());
    PROC_PORTS_TBL.setTableWidth(context.getConsole().getWidth());
    PROC_NUMA_TBL.setTableWidth(context.getConsole().getWidth());
    TITLE_TBL.setTableWidth(context.getConsole().getWidth());
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    String  dist         = null;
    String  version      = null;
    String  profile      = null;
    String  vmName       = null;
    String  pid          = null;
    String  portRange    = null;
    boolean displayPorts = false;
    boolean displayNumaNode = false;

    CmdLine cmd = ctx.getCommandLine();

    if (cmd.containsOption(OPT_CLEAN.getName(), false)) {
      ctx.getConsole().println("Wiping out inactive process info (this will remove all such references from Corus)");
      ctx.getCorus().getProcessorFacade().clean(getClusterInfo(ctx));
      return;
    }

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

    if (cmd.containsOption(OPT_PORT_RANGE.getName(), true)) {
      portRange = cmd.assertOption(OPT_PORT_RANGE.getName(), true).getValue();
    }

    displayPorts = cmd.containsOption(OPT_PORTS.getName(), false);
    displayNumaNode = cmd.containsOption(OPT_NUMA.getName(), false);

    ClusterInfo cluster = getClusterInfo(ctx);

    Results<List<Process>> res;

    if (pid != null) {
      try {
        Process proc = ctx.getCorus().getProcessorFacade().getProcess(pid);
        displayHeader(ctx.getCorus().getContext().getServerHost(), ctx, displayPorts, displayNumaNode);
        displayProcess(proc, ctx, displayPorts, displayNumaNode);
      } catch (Exception e) {
        ctx.getConsole().println(e.getMessage());
      }
    } else {
      ProcessCriteria.Builder builder = ProcessCriteria.builder().name(vmName).distribution(dist).version(version).profile(profile);
      if (portRange != null) {
        builder.ports(PortCriteria.fromLiteral(portRange));
      }
      res = ctx.getCorus().getProcessorFacade().getProcesses(builder.build(), cluster);
      res = Sorting.sortList(res, Process.class, ctx.getSortSwitches());
      displayResults(res, ctx, displayPorts, displayNumaNode);
    }
  }

  private void displayResults(Results<List<Process>> res, CliContext ctx, boolean displayPorts, boolean displayNumaNode) {
    while (res.hasNext()) {
      Result<List<Process>> result = res.next();
      displayHeader(result.getOrigin(), ctx, displayPorts, displayNumaNode);
      for (Process proc : result.getData()) {
        displayProcess(proc, ctx, displayPorts, displayNumaNode);
      }
    }
  }

  private void displayProcess(Process proc, CliContext ctx, boolean displayPorts, boolean displayNumaNode) {
    Table procTable;
    if (displayPorts) {
      procTable = PROC_PORTS_TBL.createTable(ctx.getConsole().out());
    } else if (displayNumaNode) {
      procTable = PROC_NUMA_TBL.createTable(ctx.getConsole().out());
    } else {
      procTable = PROC_TBL.createTable(ctx.getConsole().out());
    }

    procTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = procTable.newRow();
    row.getCellAt(PROC_TBL.col("dist").index()).append(proc.getDistributionInfo().getName());
    row.getCellAt(PROC_TBL.col("version").index()).append(proc.getDistributionInfo().getVersion());
    row.getCellAt(PROC_TBL.col("profile").index()).append(proc.getDistributionInfo().getProfile());
    row.getCellAt(PROC_TBL.col("name").index()).append(proc.getDistributionInfo().getProcessName());
    row.getCellAt(PROC_TBL.col("pid").index()).append(proc.getProcessID());
    if (displayPorts) {
      row.getCellAt(PROC_PORTS_TBL.col("ports").index()).append(proc.getActivePorts().toString());
    } else if (displayNumaNode) {
      row.getCellAt(PROC_NUMA_TBL.col("numanode").index()).append(proc.getNumaNode() == null? "": String.valueOf(proc.getNumaNode()));
    } else {
      row.getCellAt(PROC_TBL.col("ospid").index()).append(proc.getOsPid() == null ? "n/a" : ToStringUtil.abbreviate(proc.getOsPid(), OS_PID_LEN));
      row.getCellAt(PROC_TBL.col("status").index()).append(proc.getStatus().abbreviation());
    }
    row.flush();
  }

  private void displayHeader(CorusHost addr, CliContext ctx, boolean displayPorts, boolean displayNuma) {
    Table procTable;
    if (displayPorts) {
      procTable = PROC_PORTS_TBL.createTable(ctx.getConsole().out());
    } else if (displayNuma) {
      procTable = PROC_NUMA_TBL.createTable(ctx.getConsole().out());
    } else {
      procTable = PROC_TBL.createTable(ctx.getConsole().out());
    }

    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    procTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = procTable.newRow();
    headers.getCellAt(PROC_TBL.col("dist").index()).append("Distribution");
    headers.getCellAt(PROC_TBL.col("version").index()).append("Version");
    headers.getCellAt(PROC_TBL.col("profile").index()).append("Profile");
    headers.getCellAt(PROC_TBL.col("name").index()).append("Name");
    headers.getCellAt(PROC_TBL.col("pid").index()).append("PID");
    if (displayPorts) {
      headers.getCellAt(PROC_PORTS_TBL.col("ports").index()).append("Ports");
    } else if (displayNuma) {
      headers.getCellAt(PROC_NUMA_TBL.col("numanode").index()).append("Numa Node");
    } else {
      headers.getCellAt(PROC_TBL.col("ospid").index()).append("OS PID");
      headers.getCellAt(PROC_TBL.col("status").index()).append("Status");
    }
    headers.flush();
  }
}
