package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Acquires diagnostic data from Corus.
 * 
 * @author yduchesne
 *
 */
public class Diags extends CorusCliCommand {

  private static final OptionDef OPT_ABORT_ENABLED = new OptionDef("a", false);
  private static final OptionDef OPT_POLL_INTERVAL = new OptionDef("t", true);
  private static final OptionDef OPT_SILENT_MODE   = new OptionDef("s", false);
  private static final OptionDef OPT_WAIT          = new OptionDef("w", false);

  
  private static final List<OptionDef> AVAILABLE_OPTIONS = Collects.arrayToList(
      OPT_CLUSTER, OPT_ABORT_ENABLED, OPT_POLL_INTERVAL, OPT_WAIT, 
      OPT_DIST, OPT_VERSION, OPT_PROCESS_NAME 
  );
  
  private static final int DEFAULT_INTERVAL_SECONDS = 10;
    
  private TableDef DIST_TBL = TableDef.newInstance()
      .createCol("status", 18)
      .createCol("dist", 18)
      .createCol("version", 8)
      .createCol("processes", 18)
      .createCol("protocol", 8);

  private TableDef MSG_TBL = TableDef.newInstance()
      .createCol("msg", 78);
  
  private TableDef TITLE_TBL  = TableDef.newInstance()
      .createCol("val", 78);

  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAILABLE_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {

    boolean wait         = ctx.getCommandLine().containsOption(OPT_WAIT.getName(), false);
    boolean abortOnError = ctx.getCommandLine().containsOption(OPT_ABORT_ENABLED.getName(), false);
    boolean silentMode   = ctx.getCommandLine().containsOption(OPT_SILENT_MODE.getName(), false);
    
    int pollInterval = DEFAULT_INTERVAL_SECONDS;
    
    if (ctx.getCommandLine().containsOption(OPT_POLL_INTERVAL.getName(), false)) {
      pollInterval = Integer.parseInt(ctx.getCommandLine().assertOption(OPT_POLL_INTERVAL.getName(), true).getValue());
    }

    int globalDiagnosticErrorCount, globalDiagnosticPendingCount;
    int progressDiagnosticErrorCount;
    
    List<PairTuple<CorusHost, List<ProcessConfigDiagnosticResult>>> processDiagnosticResults = new ArrayList<>();
    List<PairTuple<CorusHost, List<String>>> progressDiagnosticResults = new ArrayList<>();
    Map<CorusHost, GlobalDiagnosticResult> globalResultsByHost = new HashMap<CorusHost, GlobalDiagnosticResult>();
    
    do {
      globalDiagnosticErrorCount     = 0;
      globalDiagnosticPendingCount   = 0;
      progressDiagnosticErrorCount   = 0;
      processDiagnosticResults.clear();
      progressDiagnosticResults.clear();
      
      Results<GlobalDiagnosticResult> results = ctx.getCorus().getDiagnosticFacade().acquireDiagnostics(getProcessCriteria(ctx), getClusterInfo(ctx));
      
      while (results.hasNext()) {
        Result<GlobalDiagnosticResult> globalDiag = results.next();
        globalResultsByHost.put(globalDiag.getOrigin(), globalDiag.getData());
        if (globalDiag.getData().getStatus() == GlobalDiagnosticStatus.FAILURE) {
          globalDiagnosticErrorCount++;
        } else if (globalDiag.getData().getStatus() == GlobalDiagnosticStatus.INCOMPLETE) {
          globalDiagnosticPendingCount++;
        }
        
        PairTuple<CorusHost, List<ProcessConfigDiagnosticResult>> hostProcessDiag = 
            new PairTuple<CorusHost, List<ProcessConfigDiagnosticResult>>(
                globalDiag.getOrigin(), globalDiag.getData().getProcessResults()
            );
        
        processDiagnosticResults.add(hostProcessDiag);
        
        PairTuple<CorusHost, List<String>> hostProgressDiag =
            new PairTuple<CorusHost, List<String>>(
                globalDiag.getOrigin(), globalDiag.getData().getProgressResult().getErrorMessages()
            );
        progressDiagnosticResults.add(hostProgressDiag);
        if (!globalDiag.getData().getProgressResult().getErrorMessages().isEmpty()) {
          progressDiagnosticErrorCount++;
        }
      }
      
      if (!wait || globalDiagnosticErrorCount > 0 || globalDiagnosticPendingCount == 0) {
        break;
      }
      
      sleep(TimeUnit.SECONDS.toMillis(pollInterval));
    } while (true);

    if (!silentMode) {
      if (progressDiagnosticErrorCount > 0) {
        for (PairTuple<CorusHost, List<String>> result : progressDiagnosticResults) {
          displayProgressDiagnosticsHeader(result.getLeft(), globalResultsByHost.get(result.getLeft()).getStatus(), ctx);
          displayProgressDiagnostics(result.getRight(), ctx);
        }
      } else {
        for (PairTuple<CorusHost, List<ProcessConfigDiagnosticResult>> result : processDiagnosticResults) {
          displayProcessDiagnosticsHeader(result.getLeft(), globalResultsByHost.get(result.getLeft()).getStatus(), ctx);
          for (ProcessConfigDiagnosticResult diagnosticResult : result.getRight()) {
            displayProcessDiagnostics(diagnosticResult, ctx);
          }
        }
      }
    }
    
    if (abortOnError && globalDiagnosticErrorCount > 0) {
      throw new AbortException("Unstable Corus node(s) detected");
    }
  }
  
  private void displayProcessDiagnosticsHeader(CorusHost addr, GlobalDiagnosticStatus status, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = DIST_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ")
      .append(addr.getFormattedAddress())
      .append(" - Host diagnostic: ")
      .append(status.name());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();
    headers.getCellAt(DIST_TBL.col("status").index()).append("Status");
    headers.getCellAt(DIST_TBL.col("dist").index()).append("Distribution");
    headers.getCellAt(DIST_TBL.col("version").index()).append("Version");
    headers.getCellAt(DIST_TBL.col("processes").index()).append("Process");
    headers.getCellAt(DIST_TBL.col("protocol").index()).append("Protocol");

    headers.flush();
  }
  
  private void displayProcessDiagnostics(ProcessConfigDiagnosticResult diagnosticResult, CliContext ctx) {
    Table distTable = DIST_TBL.createTable(ctx.getConsole().out());

    distTable.drawLine('-', 0, ctx.getConsole().getWidth());
    
    Row row = distTable.newRow();
    row.getCellAt(DIST_TBL.col("status").index()).append(diagnosticResult.getStatus().name());
    row.getCellAt(DIST_TBL.col("dist").index()).append(diagnosticResult.getDistribution().getName());
    row.getCellAt(DIST_TBL.col("version").index()).append(diagnosticResult.getDistribution().getVersion());
    row.getCellAt(DIST_TBL.col("processes").index()).append(diagnosticResult.getProcessConfig().getName());
    row.flush();
    
    if (!diagnosticResult.getProcessResults().isEmpty()) {
      distTable.newRow().flush();
    }
    
    for (ProcessDiagnosticResult pdr : diagnosticResult.getProcessResults()) {
      row = distTable.newRow();
      row.getCellAt(DIST_TBL.col("status").index()).append(pdr.getStatus().name().toLowerCase().replace('_', ' '));
      row.getCellAt(DIST_TBL.col("processes").index()).append(pdr.getProcess().getProcessID());
      row.getCellAt(DIST_TBL.col("protocol").index()).append(pdr.getProtocol().isSet() ? pdr.getProtocol().get() : "N/A");
      row.flush();
    }
    
  }

  private void displayProgressDiagnosticsHeader(CorusHost addr, GlobalDiagnosticStatus status, CliContext ctx) {
    Table titleTable   = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = MSG_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ")
      .append(addr.getFormattedAddress())
      .append(" - Host diagnostic: ")
      .append(status.name());
    row.flush();


    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();
    headers.getCellAt(MSG_TBL.col("msg").index()).append("Message");
    headers.flush();
  }

  private void displayProgressDiagnostics(List<String> messages, CliContext ctx) {
    Table msgTable = MSG_TBL.createTable(ctx.getConsole().out());

    msgTable.drawLine('-', 0, ctx.getConsole().getWidth());
    
    for (String pdr : messages) {
      Row row = msgTable.newRow();
      row.getCellAt(MSG_TBL.col("msg").index()).append(pdr);
      row.flush();
    }
  }
  
  private ProcessCriteria getProcessCriteria(CliContext ctx) {
    ProcessCriteria.Builder builder = ProcessCriteria.builder();
    CmdLine cmd = ctx.getCommandLine();
    if (cmd.containsOption(OPT_DIST.getName(), true)) {
      builder.distribution(ArgMatchers.parse(cmd.assertOption(OPT_DIST.getName(), true).getValue()));
    }

    if (cmd.containsOption(OPT_VERSION.getName(), true)) {
      builder.version(ArgMatchers.parse(cmd.assertOption(OPT_VERSION.getName(), true).getValue()));
    }

    if (cmd.containsOption(OPT_PROCESS_NAME.getName(), true)) {
      builder.name(ArgMatchers.parse(cmd.assertOption(OPT_PROCESS_NAME.getName(), true).getValue()));
    }
    return builder.build();
  }
}
