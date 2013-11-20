package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Cell;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.ubik.net.ServerAddress;

/**
 * Displays distribution info.
 * 
 * @author Yanick Duchesne
 */
public class Ls extends CorusCliCommand {

  // --------------------------------------------------------------------------

  private static TableDef DIST_TBL = TableDef.newInstance().createCol("dist", 18).createCol("version", 8).createCol("processes", 23)
      .createCol("profiles", 23);

  private static TableDef EXE_TBL = TableDef.newInstance().createCol("name", 18).createCol("boot", 4).createCol("dist", 18).createCol("version", 8)
      .createCol("process", 10).createCol("profile", 10);

  private static TableDef SCRIPT_TBL = TableDef.newInstance().createCol("alias", 25).createCol("file", 20).createCol("desc", 31);

  private static TableDef FILE_TBL = TableDef.newInstance().createCol("file", 25).createCol("len", 20).createCol("date", 31);

  private static TableDef TITLE_TBL = TableDef.newInstance().createCol("val", 78);

  // --------------------------------------------------------------------------

  private static final String OPT_EXEC_CONFIG = "e";
  private static final String OPT_SCRIPT = "s";
  private static final String OPT_FILE = "f";

  // --------------------------------------------------------------------------

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    if (ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, false)) {
      doListExecConfigs(ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_SCRIPT, false)) {
      doListScripts(ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_FILE, false)) {
      doListFiles(ctx);
    } else {
      doListDistributions(ctx);
    }
  }

  private void doListExecConfigs(CliContext ctx) throws InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    try {
      Results<List<ExecConfig>> res = ctx.getCorus().getProcessorFacade().getExecConfigs(cluster);
      while (res.hasNext()) {
        Result<List<ExecConfig>> result = res.next();
        displayExecConfigHeader(result.getOrigin(), ctx);
        for (ExecConfig conf : result.getData()) {
          displayExecConfig(conf, ctx);
        }
      }

    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem listing execution configurations", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  private void doListScripts(CliContext ctx) throws InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    ShellScriptCriteria criteria = ShellScriptCriteria.newInstance();
    if (ctx.getCommandLine().containsOption(OPT_SCRIPT, true)) {
      criteria.setAlias(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_SCRIPT, true).getValue()));
    }

    try {
      Results<List<ShellScript>> res = ctx.getCorus().getScriptManagementFacade().getScripts(criteria, cluster);
      while (res.hasNext()) {
        Result<List<ShellScript>> result = res.next();
        displayScriptHeader(result.getOrigin(), ctx);
        for (ShellScript s : result.getData()) {
          displayScript(s, ctx);
        }
      }

    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem listing execution configurations", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  private void doListFiles(CliContext ctx) throws InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    FileCriteria criteria = FileCriteria.newInstance();
    if (ctx.getCommandLine().containsOption(OPT_FILE, true)) {
      criteria.setName(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_FILE, true).getValue()));
    }

    try {
      Results<List<FileInfo>> res = ctx.getCorus().getFileManagementFacade().getFiles(criteria, cluster);
      while (res.hasNext()) {
        Result<List<FileInfo>> result = res.next();
        displayFileHeader(result.getOrigin(), ctx);
        for (FileInfo s : result.getData()) {
          displayFile(s, ctx);
        }
      }

    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem listing execution configurations", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  private void doListDistributions(CliContext ctx) throws AbortException, InputException {
    String dist = null;
    String version = null;
    CmdLine cmd = ctx.getCommandLine();
    if (cmd.containsOption(DIST_OPT, true)) {
      dist = cmd.assertOption(DIST_OPT, true).getValue();
    }

    if (cmd.containsOption(VERSION_OPT, true)) {
      version = cmd.assertOption(VERSION_OPT, true).getValue();
    }

    ClusterInfo cluster = getClusterInfo(ctx);
    DistributionCriteria criteria = DistributionCriteria.builder().name(dist).version(version).build();
    Results<List<Distribution>> res = ctx.getCorus().getDeployerFacade().getDistributions(criteria, cluster);
    displayResults(res, ctx);
  }

  private void displayResults(Results<List<Distribution>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<Distribution>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for (Distribution dist : result.getData()) {
        displayDist(dist, ctx);
      }
    }
  }

  private void displayExecConfig(ExecConfig conf, CliContext ctx) {
    Table table = EXE_TBL.createTable(ctx.getConsole().out());

    table.drawLine('-', 0, CONSOLE_WIDTH);

    int count = 0;
    for (ProcessDef def : conf.getProcesses()) {
      Row row = table.newRow();
      if (count == 0) {
        row.getCellAt(EXE_TBL.col("name").index()).append(conf.getName());
        if (conf.isStartOnBoot()) {
          row.getCellAt(EXE_TBL.col("boot").index()).append("y");
        } else {
          row.getCellAt(EXE_TBL.col("boot").index()).append("n");
        }
      } else {
        row.getCellAt(EXE_TBL.col("name").index()).append("");
        row.getCellAt(EXE_TBL.col("boot").index()).append("");
      }

      row.getCellAt(EXE_TBL.col("dist").index()).append(def.getDist());
      row.getCellAt(EXE_TBL.col("version").index()).append(def.getVersion());
      row.getCellAt(EXE_TBL.col("process").index()).append(def.getName());
      row.getCellAt(EXE_TBL.col("profile").index()).append(def.getProfile());
      row.flush();
      count++;
    }
  }

  private void displayScript(ShellScript script, CliContext ctx) {
    Table scriptTable = SCRIPT_TBL.createTable(ctx.getConsole().out());

    scriptTable.drawLine('-', 0, CONSOLE_WIDTH);

    Row row = scriptTable.newRow();
    row.getCellAt(SCRIPT_TBL.col("alias").index()).append(script.getAlias());
    row.getCellAt(SCRIPT_TBL.col("file").index()).append(script.getFileName());
    row.getCellAt(SCRIPT_TBL.col("desc").index()).append(script.getDescription());
    row.flush();
  }

  private void displayFile(FileInfo file, CliContext ctx) {
    Table fileTable = FILE_TBL.createTable(ctx.getConsole().out());

    fileTable.drawLine('-', 0, CONSOLE_WIDTH);

    Row row = fileTable.newRow();
    row.getCellAt(FILE_TBL.col("file").index()).append(file.getName());
    row.getCellAt(FILE_TBL.col("len").index()).append(String.valueOf(file.getLength()));
    row.getCellAt(FILE_TBL.col("date").index()).append(file.getLastModified().toString());
    row.flush();
  }

  private void displayDist(Distribution dist, CliContext ctx) {
    Table distTable = DIST_TBL.createTable(ctx.getConsole().out());

    distTable.drawLine('-', 0, CONSOLE_WIDTH);

    Row row = distTable.newRow();
    row.getCellAt(DIST_TBL.col("dist").index()).append(dist.getName());
    row.getCellAt(DIST_TBL.col("version").index()).append(dist.getVersion());
    List<ProcessConfig> vms = dist.getProcesses();

    // StringBuffer profiles = new StringBuffer();
    List<String> profiles;

    for (int k = 0; k < vms.size(); k++) {
      Cell cell = row.getCellAt(DIST_TBL.col("processes").index());
      ProcessConfig vm = (ProcessConfig) vms.get(k);
      cell.append(vm.getName());
      profiles = vm.getProfiles();

      for (int p = 0; p < profiles.size(); p++) {
        row.getCellAt(DIST_TBL.col("profiles").index()).append((String) profiles.get(p));

        if (p < (profiles.size() - 1)) {
          row.getCellAt(DIST_TBL.col("profiles").index()).append(", ");
        }
      }

      row.flush();

      row = distTable.newRow();
    }

    row.flush();
  }

  private void displayHeader(ServerAddress addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = DIST_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, CONSOLE_WIDTH);

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(ctx.getCorus().getContext().resolve(addr).getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, CONSOLE_WIDTH);

    Row headers = headersTable.newRow();
    headers.getCellAt(DIST_TBL.col("dist").index()).append("Distribution");
    headers.getCellAt(DIST_TBL.col("version").index()).append("Version");
    headers.getCellAt(DIST_TBL.col("processes").index()).append("Process Configs");
    headers.getCellAt(DIST_TBL.col("profiles").index()).append("Profiles");
    headers.flush();
  }

  private void displayExecConfigHeader(ServerAddress addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = EXE_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, CONSOLE_WIDTH);
    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(ctx.getCorus().getContext().resolve(addr).getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, CONSOLE_WIDTH);

    Row headers = headersTable.newRow();
    headers.getCellAt(EXE_TBL.col("name").index()).append("Name");
    headers.getCellAt(EXE_TBL.col("boot").index()).append("Boot");
    headers.getCellAt(EXE_TBL.col("dist").index()).append("Distribution");
    headers.getCellAt(EXE_TBL.col("version").index()).append("Version");
    headers.getCellAt(EXE_TBL.col("process").index()).append("Process");
    headers.getCellAt(EXE_TBL.col("profile").index()).append("Profile");
    headers.flush();
  }

  private void displayScriptHeader(ServerAddress addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = SCRIPT_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, CONSOLE_WIDTH);
    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(ctx.getCorus().getContext().resolve(addr).getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, CONSOLE_WIDTH);

    Row headers = headersTable.newRow();
    headers.getCellAt(SCRIPT_TBL.col("alias").index()).append("Alias");
    headers.getCellAt(SCRIPT_TBL.col("file").index()).append("File");
    headers.getCellAt(SCRIPT_TBL.col("desc").index()).append("Description");
    headers.flush();
  }

  private void displayFileHeader(ServerAddress addr, CliContext ctx) {
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = FILE_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, CONSOLE_WIDTH);
    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(ctx.getCorus().getContext().resolve(addr).getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, CONSOLE_WIDTH);

    Row headers = headersTable.newRow();
    headers.getCellAt(FILE_TBL.col("file").index()).append("File");
    headers.getCellAt(FILE_TBL.col("len").index()).append("Length");
    headers.getCellAt(FILE_TBL.col("date").index()).append("Last Modified");
    headers.flush();
  }
}
