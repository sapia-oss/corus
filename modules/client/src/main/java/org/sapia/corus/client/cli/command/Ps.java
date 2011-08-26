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
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
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
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    String  profile = null;
    String  vmName  = null;
    String  pid = null;
    boolean displayPorts = false;

    CmdLine cmd = ctx.getCommandLine();

    if (cmd.containsOption(DIST_OPT, true)) {
      dist = cmd.assertOption(DIST_OPT, true).getValue();
    }

    if (cmd.containsOption(VERSION_OPT, true)) {
      version = cmd.assertOption(VERSION_OPT, true).getValue();
    }

    if (cmd.containsOption(PROFILE_OPT, true)) {
      profile = cmd.assertOption(PROFILE_OPT, true).getValue();
    }

    if (cmd.containsOption(VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(VM_ID_OPT, true)) {
      pid = cmd.assertOption(VM_ID_OPT, true).getValue();
    }
    
    displayPorts = cmd.containsOption(OPT_PORTS, false);

    ClusterInfo cluster = getClusterInfo(ctx);

    Results<List<Process>> res;

    if (pid != null) {
      try {
        Process proc = ctx.getCorus().getProcessorFacade().getProcess(pid);
        displayHeader(ctx.getCorus().getContext().getAddress(), ctx, displayPorts);
        displayProcess(proc, ctx, displayPorts);
      } catch (Exception e) {
        ctx.getConsole().println(e.getMessage());
      }
    } else {
      ProcessCriteria criteria = ProcessCriteria.builder()
        .name(vmName)
        .distribution(dist)
        .version(version)
        .profile(profile)
        .build();
        
      res = ctx.getCorus().getProcessorFacade().getProcesses(criteria, cluster);
      displayResults(res, ctx, displayPorts);
    }
  }

  private void displayResults(Results<List<Process>> res, CliContext ctx, boolean displayPorts) {
    
    while (res.hasNext()) {
      Result<List<Process>> result = res.next();
      displayHeader(result.getOrigin(), ctx, displayPorts);
      for(Process proc:result.getData()){
        displayProcess(proc, ctx, displayPorts);
      }
    }
  }

  private void displayProcess(Process proc, CliContext ctx, boolean displayPorts) {
    Table   procTable;
    Row     row;

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

      if(proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED || 
         proc.getStatus() == Process.LifeCycleStatus.KILL_REQUESTED){
        row.getCellAt(COL_STATUS).append(TERMINATING);      
      }
      else if(proc.getStatus() == Process.LifeCycleStatus.SUSPENDED){
        row.getCellAt(COL_STATUS).append(SUSPENDED);      
      }
      else if(proc.getStatus() == Process.LifeCycleStatus.RESTARTING){
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

