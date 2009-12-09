package org.sapia.corus.admin.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Cell;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.services.processor.ProcStatus;
import org.sapia.corus.exceptions.LogicException;
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
    String  vmId    = null;

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

    ClusterInfo cluster = getClusterInfo(ctx);

    Results res;

    if (vmId != null) {
      try {
        ProcStatus stat = ctx.getCorus().getStatusFor(vmId);
        displayHeader(ctx.getCorus().getServerAddress(), ctx);
        displayStatus(stat, ctx);
      } catch (LogicException e) {
        ctx.getConsole().println(e.getMessage());
      }
    } else if ((dist != null) && (version != null) && (profile != null) &&
                 (vmName != null)) {
      res = ctx.getCorus().getStatus(dist, version, profile, vmName, cluster);
      displayResults(res, ctx);
    } else if ((dist != null) && (version != null) && (profile != null)) {
      res = ctx.getCorus().getStatus(dist, version, profile, cluster);
      displayResults(res, ctx);
    } else if ((dist != null) && (version != null)) {
      res = ctx.getCorus().getStatus(dist, version, cluster);
      displayResults(res, ctx);
    } else if (dist != null) {
      res = ctx.getCorus().getStatus(dist, cluster);
      displayResults(res, ctx);
    } else {
      res = ctx.getCorus().getStatus(cluster);
      displayResults(res, ctx);
    }
  }

  private void displayResults(Results res, CliContext ctx) {
    HostList dists;
    ProcStatus stat;

    while (res.hasNext()) {
      dists = (HostList) res.next();

      if (dists.size() > 0) {
        displayHeader(dists.getServerAddress(), ctx);

        for (int j = 0; j < dists.size(); j++) {
          stat = (ProcStatus) dists.get(j);
          displayStatus(stat, ctx);
        }
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
    List contexts = stat.getContexts();
    if(contexts.size() == 0){
      row.flush();
      return;
    }
    Context context;
    for(int i = 0; i < contexts.size(); i++){
      context = (Context)contexts.get(i);
      row.getCellAt(COL_CONTEXT).append(context.getName());
      List params = context.getParams();
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

    headers.getCellAt(COL_PID).append("Dyn. PID");
    headers.getCellAt(COL_CONTEXT).append("Context");
    headers.getCellAt(COL_PARAM_NAME).append("Name");
    headers.getCellAt(COL_PARAM_VALUE).append("Value");
    headers.flush();
  }
}

