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
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.ubik.net.ServerAddress;

/**
 * Displays process status.
 * 
 * @author Yanick Duchesne
 */
public class Status extends CorusCliCommand {
  
  private static final TableDef STAT_TBL = TableDef.newInstance()
      .createCol("pid", 15)
      .createCol("context", 15)
      .createCol("name", 15)
      .createCol("value", 25);
  
  private static final TableDef TITLE_TBL = TableDef.newInstance()
      .createCol("val", 78);  
  
  // --------------------------------------------------------------------------

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
    Table   procTable = STAT_TBL.createTable(ctx.getConsole().out());

    procTable.drawLine('-', 0, CONSOLE_WIDTH);

    Row row = procTable.newRow();
    row.getCellAt(STAT_TBL.col("pid").index()).append(stat.getProcessID());
    List<Context> contexts = stat.getContexts();
    if(contexts.size() == 0){
      row.flush();
      return;
    }
    Context context;
    for(int i = 0; i < contexts.size(); i++){
      context = (Context)contexts.get(i);
      row.getCellAt(STAT_TBL.col("context").index()).append(context.getName());
      List<Param> params = context.getParams();
      if(params.size() == 0){
        row.flush();
        row = procTable.newRow();
        continue;
      }
      Param param;
      for(int j = 0; j < params.size(); j++){
        param = (Param)params.get(j);
        row.getCellAt(STAT_TBL.col("name").index()).append(param.getName());        
        row.getCellAt(STAT_TBL.col("value").index()).append(param.getValue());
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
    Table procTable  = STAT_TBL.createTable(ctx.getConsole().out());
    Table titleTable = TITLE_TBL.createTable(ctx.getConsole().out());
    
    procTable.drawLine('=', 0, CONSOLE_WIDTH);
    
    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.toString());
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

