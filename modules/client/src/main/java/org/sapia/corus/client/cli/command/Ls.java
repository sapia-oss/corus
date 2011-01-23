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
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 */
public class Ls extends CorusCliCommand {
  
  private static final String OPT_EXEC_CONFIG = "e";
  
  private static final int COL_EXEC_NAME     = 0;
  private static final int COL_EXEC_BOOT     = 1;
  private static final int COL_EXEC_DIST     = 2;
  private static final int COL_EXEC_VERSION  = 3;
  private static final int COL_EXEC_PROCESS  = 4;
  private static final int COL_EXEC_PROFILE  = 5;
  
  private static final int COL_DIST     = 0;
  private static final int COL_VERSION  = 1;
  private static final int COL_VMS      = 2;
  private static final int COL_PROFILES = 3;

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, false)){
      doListExecConfigs(ctx);
    }
    else{
      doListDistributions(ctx);
    }
  }
   
  private void doListExecConfigs(CliContext ctx){
    ClusterInfo cluster = getClusterInfo(ctx);
    try {
      Results<List<ExecConfig>> res = ctx.getCorus().getProcessorFacade().getExecConfigs(cluster);
      while (res.hasNext()) {
        Result<List<ExecConfig>> result = res.next();
        displayExecConfigHeader(result.getOrigin(), ctx);
        for(ExecConfig conf:result.getData()){
          displayExecConfig(conf, ctx);
        }
      }
      
    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem listing execution configurations", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }
  
  private void doListDistributions(CliContext ctx) throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    CmdLine cmd     = ctx.getCommandLine();
    if (cmd.containsOption(DIST_OPT, true)) {
      dist = cmd.assertOption(DIST_OPT, true).getValue();
    }

    if (cmd.containsOption(VERSION_OPT, true)) {
      version = cmd.assertOption(VERSION_OPT, true).getValue();
    }

    ClusterInfo cluster = getClusterInfo(ctx);

    if ((dist != null) && (version != null)) {
      Results<List<Distribution>> res = ctx.getCorus().getDeployerFacade().getDistributions(dist, version, cluster);
      displayResults(res, ctx);
    } else if (dist != null) {
      Results<List<Distribution>> res = ctx.getCorus().getDeployerFacade().getDistributions(dist, cluster);
      displayResults(res, ctx);
    } else {
      Results<List<Distribution>> res = ctx.getCorus().getDeployerFacade().getDistributions(cluster);
      displayResults(res, ctx);
    }
  }

  private void displayResults(Results<List<Distribution>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<Distribution>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for(Distribution dist:result.getData()){
        displayDist(dist, ctx);
      }
    }
  }
  
  
  private void displayExecConfig(ExecConfig conf, CliContext ctx) {
    Table         distTable = new Table(ctx.getConsole().out(), 6, 40);
    Row           row;

    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_NAME).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_BOOT).setWidth(4);

    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_DIST).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_VERSION).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_PROCESS).setWidth(10);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_PROFILE).setWidth(10);

    distTable.drawLine('-');
    
    int count = 0;
    for(ProcessDef def:conf.getProcesses()){
      row = distTable.newRow();
      if(count == 0){
        row.getCellAt(COL_EXEC_NAME).append(conf.getName());
        if(conf.isStartOnBoot()){
          row.getCellAt(COL_EXEC_BOOT).append("y");
        }
        else{
          row.getCellAt(COL_EXEC_BOOT).append("n");
        }
      }
      else{
        row.getCellAt(COL_EXEC_NAME).append("");
        row.getCellAt(COL_EXEC_BOOT).append("");
      }
    
      row.getCellAt(COL_EXEC_DIST).append(def.getDist());
      row.getCellAt(COL_EXEC_VERSION).append(def.getVersion());
      row.getCellAt(COL_EXEC_PROCESS).append(def.getName());
      row.getCellAt(COL_EXEC_PROFILE).append(def.getProfile());
      row.flush();
      count++;
    }
  }

  private void displayDist(Distribution dist, CliContext ctx) {
    Table               distTable = new Table(ctx.getConsole().out(), 4, 20);
    Row                 row;
    List<ProcessConfig> vms;
    Cell                cell;
    ProcessConfig       vm;

    distTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_VMS).setWidth(23);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILES).setWidth(23);

    distTable.drawLine('-');

    row = distTable.newRow();
    row.getCellAt(COL_DIST).append(dist.getName());
    row.getCellAt(COL_VERSION).append(dist.getVersion());
    vms = dist.getProcesses();

    //    StringBuffer profiles = new StringBuffer();
    List<String> profiles;

    for (int k = 0; k < vms.size(); k++) {
      cell = row.getCellAt(COL_VMS);
      vm   = (ProcessConfig) vms.get(k);
      cell.append(vm.getName());
      profiles = vm.getProfiles();

      for (int p = 0; p < profiles.size(); p++) {
        row.getCellAt(COL_PROFILES).append((String) profiles.get(p));

        if (p < (profiles.size() - 1)) {
          row.getCellAt(COL_PROFILES).append(", ");
        }
      }

      row.flush();

      row = distTable.newRow();
    }

    //		row.getCellAt(COL_PROFILES).append("dev, test, mig, prod, dummy");
    row.flush();
  }

  private void displayHeader(ServerAddress addr, CliContext ctx) {
    Table         hostTable;
    Table         distTable;
    Row           row;
    Row           headers;

    hostTable = new Table(ctx.getConsole().out(), 1, 78);
    hostTable.drawLine('=');
    row = hostTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    hostTable.drawLine(' ');

    distTable = new Table(ctx.getConsole().out(), 4, 20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_VMS).setWidth(23);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILES).setWidth(23);

    headers = distTable.newRow();

    headers.getCellAt(COL_DIST).append("Distribution");
    headers.getCellAt(COL_VERSION).append("Version");
    headers.getCellAt(COL_VMS).append("Process Configs");
    headers.getCellAt(COL_PROFILES).append("Profiles");
    headers.flush();
  }
  
  private void displayExecConfigHeader(ServerAddress addr, CliContext ctx) {
    Table         hostTable;
    Table         distTable;
    Row           row;
    Row           headers;

    hostTable = new Table(ctx.getConsole().out(), 1, 78);
    hostTable.drawLine('=');
    row = hostTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    hostTable.drawLine(' ');

    distTable = new Table(ctx.getConsole().out(), 6, 20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_NAME).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_BOOT).setWidth(4);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_DIST).setWidth(18);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_VERSION).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_PROCESS).setWidth(10);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_EXEC_PROFILE).setWidth(10);

    headers = distTable.newRow();

    headers.getCellAt(COL_EXEC_NAME).append("Name");
    headers.getCellAt(COL_EXEC_BOOT).append("Boot");
    headers.getCellAt(COL_EXEC_DIST).append("Distribution");
    headers.getCellAt(COL_EXEC_VERSION).append("Version");
    headers.getCellAt(COL_EXEC_PROCESS).append("Process");
    headers.getCellAt(COL_EXEC_PROFILE).append("Profile");
    headers.flush();
  }
}
