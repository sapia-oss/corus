package org.sapia.corus.admin.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Cell;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.HostItem;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.ExecConfig;
import org.sapia.corus.admin.services.processor.ProcessDef;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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

  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
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
    try{
      Results res = ctx.getCorus().getExecConfigs(cluster);
      
      ExecConfig conf;
      while (res.hasNext()) {
        Object result = res.next();
        if(result instanceof HostItem){
          HostItem item = (HostItem) result;
          displayExecConfigHeader(item.getServerAddress(), ctx);
          conf = (ExecConfig)item.get();
          displayExecConfig(conf, ctx);
        }
        else{
          HostList     confs = (HostList) result;
          if (confs.size() > 0) {
            displayExecConfigHeader(confs.getServerAddress(), ctx);
    
            for (int j = 0; j < confs.size(); j++) {
              conf = (ExecConfig) confs.get(j);
              displayExecConfig(conf, ctx);
            }
          }
        }
      }
    }catch(Exception e){
      ctx.getConsole().println("Problem listing execution configurations");
      e.printStackTrace(ctx.getConsole().out());
    }
  }
  
  private void doListDistributions(CliContext ctx)
  throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    CmdLine cmd     = ctx.getCommandLine();
    if (cmd.containsOption(super.DIST_OPT, true)) {
      dist = cmd.assertOption(super.DIST_OPT, true).getValue();
    }

    if (cmd.containsOption(super.VERSION_OPT, true)) {
      version = cmd.assertOption(super.VERSION_OPT, true).getValue();
    }

    ClusterInfo cluster = getClusterInfo(ctx);

    if ((dist != null) && (version != null)) {
      Results res = ctx.getCorus().getDistributions(dist, version, cluster);
      displayResults(res, ctx);
    } else if (dist != null) {
      Results res = ctx.getCorus().getDistributions(dist, cluster);
      displayResults(res, ctx);
    } else {
      Results res = ctx.getCorus().getDistributions(cluster);
      displayResults(res, ctx);
    }
  }

  private void displayResults(Results res, CliContext ctx) {
    Distribution dist;
    while (res.hasNext()) {
      Object result = res.next();
      if(result instanceof HostItem){
        HostItem item = (HostItem) result;
        displayHeader(item.getServerAddress(), ctx);
        displayDist((Distribution)item.get(), ctx);
      }
      else{
        HostList     dists = (HostList) result;
        if (dists.size() > 0) {
          displayHeader(dists.getServerAddress(), ctx);
  
          for (int j = 0; j < dists.size(); j++) {
            dist = (Distribution) dists.get(j);
            displayDist(dist, ctx);
          }
        }
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
    Table         distTable = new Table(ctx.getConsole().out(), 4, 20);
    Row           row;
    List          vms;
    Cell          cell;
    ProcessConfig vm;

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
    List profiles;

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
