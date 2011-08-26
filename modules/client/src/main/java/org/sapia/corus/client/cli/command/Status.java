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
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.ubik.net.ServerAddress;

/**
 * @author Yanick Duchesne
 */
public class Status extends CorusCliCommand {
  private static final int COL_PID          = 0;
  private static final int COL_CONTEXT        = 1;
  private static final int COL_PARAM_NAME    = 2;
  private static final int COL_PARAM_VALUE   = 3;  

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    String  profile = null;
    String  vmName  = null;
    String  pid     = null;

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

    ClusterInfo cluster = getClusterInfo(ctx);

    Results<List<ProcStatus>> res;

    if (pid != null) {
      try {
        ProcStatus stat = ctx.getCorus().getProcessorFacade().getStatusFor(pid);
        displayHeader(ctx.getCorus().getContext().getAddress(), ctx);
        displayStatus(stat, ctx);
      } catch (ProcessNotFoundException e) {
        throw new InputException(e.getMessage());
      }
    } else {
      ProcessCriteria criteria = ProcessCriteria.builder()
        .name(vmName)
        .distribution(dist)
        .version(version)
        .profile(profile)
        .build();
      
      res = ctx.getCorus().getProcessorFacade().getStatus(criteria, cluster);
      displayResults(res, ctx);
    } 
  }

  private void displayResults(Results<List<ProcStatus>> res, CliContext ctx) {
    
    while (res.hasNext()) {
      Result<List<ProcStatus>> result = res.next();
      displayHeader(result.getOrigin(), ctx);

      for(ProcStatus stat:result.getData()){
        displayStatus(stat, ctx);
      }
    }
  }

  private void displayStatus(ProcStatus stat, CliContext ctx) {
    Table   procTable;
    Row     row;

    procTable = new Table(ctx.getConsole().out(), 4, 20);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PID).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_CONTEXT).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PARAM_NAME).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PARAM_VALUE).setWidth(25);    
    procTable.drawLine('-', 0, 80);

    row = procTable.newRow();
    row.getCellAt(COL_PID).append(stat.getProcessID());
    List<Context> contexts = stat.getContexts();
    if(contexts.size() == 0){
      row.flush();
      return;
    }
    Context context;
    for(int i = 0; i < contexts.size(); i++){
      context = (Context)contexts.get(i);
      row.getCellAt(COL_CONTEXT).append(context.getName());
      List<Param> params = context.getParams();
      if(params.size() == 0){
        row.flush();
        row = procTable.newRow();
        continue;
      }
      Param param;
      for(int j = 0; j < params.size(); j++){
        param = (Param)params.get(j);
        row.getCellAt(COL_PARAM_NAME).append(param.getName());        
        row.getCellAt(COL_PARAM_VALUE).append(param.getValue());
        row.flush();
        if(j < params.size() - 1){
          row = procTable.newRow();
        }
      }
      if(i < contexts.size() - 1){
        row = procTable.newRow();
      }
    }

  }

  private void displayHeader(ServerAddress addr, CliContext ctx) {
    Table   procTable;
    Row     row;
    Row     headers;

    procTable = new Table(ctx.getConsole().out(), 1, 78);
    procTable.drawLine('=');
    row = procTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    procTable.drawLine(' ');

    procTable = new Table(ctx.getConsole().out(), 4, 20);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PID).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_CONTEXT).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PARAM_NAME).setWidth(15);
    procTable.getTableMetaData().getColumnMetaDataAt(COL_PARAM_VALUE).setWidth(25);

    headers = procTable.newRow();

    headers.getCellAt(COL_PID).append("PID");
    headers.getCellAt(COL_CONTEXT).append("Context");
    headers.getCellAt(COL_PARAM_NAME).append("Name");
    headers.getCellAt(COL_PARAM_VALUE).append("Value");
    headers.flush();
  }
}

