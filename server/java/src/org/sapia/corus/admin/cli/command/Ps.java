package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Cell;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

import org.sapia.corus.ClusterInfo;
import org.sapia.corus.LogicException;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.processor.Process;

import org.sapia.ubik.net.ServerAddress;

import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Ps extends CorusCliCommand {
  private static final int COL_DIST    = 0;
  private static final int COL_VERSION = 1;
  private static final int COL_PROFILE = 2;
  private static final int COL_VM_NAME = 3;
  private static final int COL_VM_ID   = 4;
  private static final int COL_OS_PID  = 5;
  private static final int COL_STATUS  = 6;  
  private static final int COL_PORTS   = 5;  
  
  private static final String TERMINATING = "shutd.";
  private static final String ACTIVE      = "act.";
  private static final String RESTART     = "rest.";  
  private static final String SUSPENDED   = "susp.";  
  
  private static final String OPT_PORTS = "ports";
  
  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    String  profile = null;
    String  vmName  = null;
    String  vmId    = null;
    boolean displayPorts = false;

    CmdLine cmd = ctx.getCommandLine();

    if (cmd.containsOption(super.DIST_OPT, true)) {
      dist = cmd.assertOption(super.DIST_OPT, true).getValue();
    }

    if (cmd.containsOption(super.VERSION_OPT, true)) {
      version = cmd.assertOption(super.VERSION_OPT, true).getValue();
    }

    if (cmd.containsOption(super.PROFILE_OPT, true)) {
      profile = cmd.assertOption(super.PROFILE_OPT, true).getValue();
    }

    if (cmd.containsOption(super.VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(super.VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(super.VM_ID_OPT, true)) {
      vmId = cmd.assertOption(super.VM_ID_OPT, true).getValue();
    }
    
    displayPorts = cmd.containsOption(OPT_PORTS, false);

    ClusterInfo cluster = getClusterInfo(ctx);

    Results res;

    if (vmId != null) {
      try {
        Process proc = ctx.getCorus().getProcess(vmId);
        displayHeader(ctx.getCorus().getServerAddress(), ctx, displayPorts);
        displayProcess(proc, ctx, displayPorts);
      } catch (LogicException e) {
        ctx.getConsole().println(e.getMessage());
      }
    } else if ((dist != null) && (version != null) && (profile != null) &&
                 (vmName != null)) {
      res = ctx.getCorus().getProcesses(dist, version, profile, vmName, cluster);
      displayResults(res, ctx, displayPorts);
    } else if ((dist != null) && (version != null) && (profile != null)) {
      res = ctx.getCorus().getProcesses(dist, version, profile, cluster);
      displayResults(res, ctx, displayPorts);
    } else if ((dist != null) && (version != null)) {
      res = ctx.getCorus().getProcesses(dist, version, cluster);
      displayResults(res, ctx, displayPorts);
    } else if (dist != null) {
      res = ctx.getCorus().getProcesses(dist, cluster);
      displayResults(res, ctx, displayPorts);
    } else {
      res = ctx.getCorus().getProcesses(cluster);
      displayResults(res, ctx, displayPorts);
    }
  }

  private void displayResults(Results res, CliContext ctx, boolean displayPorts) {
    HostList dists;
    Process  proc;

    while (res.hasNext()) {
      dists = (HostList) res.next();

      if (dists.size() > 0) {
        displayHeader(dists.getServerAddress(), ctx, displayPorts);

        for (int j = 0; j < dists.size(); j++) {
          proc = (Process) dists.get(j);
          displayProcess(proc, ctx, displayPorts);
        }
      }
    }
  }

  private void displayProcess(Process proc, CliContext ctx, boolean displayPorts) {
    Table   procTable;
    Row     row;
    Cell    cell;

    procTable = new Table(ctx.getConsole().out(), 7, 20);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(7);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILE).setWidth(8);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VM_NAME).setWidth(11);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VM_ID).setWidth(14);
    if(displayPorts){
     procTable.getTableMetaData().getColumnMetaDataAt(COL_PORTS).setWidth(15);  
    }
    else{
      procTable.getTableMetaData().getColumnMetaDataAt(COL_OS_PID).setWidth(6);   
      procTable.getTableMetaData().getColumnMetaDataAt(COL_STATUS).setWidth(9);  
    }
    procTable.drawLine('-', 0, 80);

    row = procTable.newRow();
    row.getCellAt(COL_DIST).append(proc.getDistributionInfo().getName());
    row.getCellAt(COL_VERSION).append(proc.getDistributionInfo().getVersion());
    row.getCellAt(COL_PROFILE).append(proc.getDistributionInfo().getProfile());
    row.getCellAt(COL_VM_NAME).append(proc.getDistributionInfo().getProcessName());
    row.getCellAt(COL_VM_ID).append(proc.getProcessID());
    if(displayPorts){    
      row.getCellAt(COL_PORTS).append(proc.getActivePorts().toString());
    }
    else{
      row.getCellAt(COL_OS_PID).append(proc.getOsPid() == null ? "n/a" : proc.getOsPid());

      if(proc.getStatus() == Process.KILL_CONFIRMED || proc.getStatus() == Process.KILL_REQUESTED){
        row.getCellAt(COL_STATUS).append(TERMINATING);      
      }
      else if(proc.getStatus() == Process.SUSPENDED){
        row.getCellAt(COL_STATUS).append(SUSPENDED);      
      }
      else if(proc.getStatus() == Process.RESTARTING){
        row.getCellAt(COL_STATUS).append(RESTART);    
      }
      else{
        row.getCellAt(COL_STATUS).append(ACTIVE);      
      }
    }
    row.flush();
  }

  private void displayHeader(ServerAddress addr, CliContext ctx, boolean displayPorts) {
    Table   procTable;
    Row     row;
    Row     headers;
    Cell    cell;
    Process vm;

    procTable = new Table(ctx.getConsole().out(), 1, 78);
    procTable.drawLine('=');
    row = procTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    procTable.drawLine(' ');

    procTable = new Table(ctx.getConsole().out(), 7, 20);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(7);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILE).setWidth(8);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VM_NAME).setWidth(11);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_VM_ID).setWidth(14);
    if(displayPorts){
      procTable.getTableMetaData().getColumnMetaDataAt(COL_PORTS).setWidth(15);    
    }
    else{
      procTable.getTableMetaData().getColumnMetaDataAt(COL_OS_PID).setWidth(6);    
      procTable.getTableMetaData().getColumnMetaDataAt(COL_STATUS).setWidth(9);    
    }

    headers = procTable.newRow();

    headers.getCellAt(COL_DIST).append("Distribution");
    headers.getCellAt(COL_VERSION).append("Version");
    headers.getCellAt(COL_PROFILE).append("Profile");
    headers.getCellAt(COL_VM_NAME).append("Name");
    headers.getCellAt(COL_VM_ID).append("Process ID");
    if(displayPorts){
      headers.getCellAt(COL_PORTS).append("Ports");    
    }
    else{
      headers.getCellAt(COL_OS_PID).append("OS PID");    
      headers.getCellAt(COL_STATUS).append("Status");          
    }
    headers.flush();
  }
}

