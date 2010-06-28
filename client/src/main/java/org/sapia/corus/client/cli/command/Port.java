package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.ubik.net.ServerAddress;

/**
 *
 * @author yduchesne
 */
public class Port extends CorusCliCommand{
  
  public static final String ADD = "add";
  public static final String DELETE = "del";
  public static final String RELEASE = "release";
  public static final String LIST  = "ls";
  public static final String OPT_NAME = "n";
  public static final String OPT_FORCE = "f";
  public static final String OPT_MIN = "min";
  public static final String OPT_MAX = "max";
  
  public static final int COL_NAME   = 0;
  public static final int COL_RANGE  = 1;
  public static final int COL_AVAIL  = 2;
  public static final int COL_ACTIVE = 3;
  
  /** Creates a new instance of Port */
  public Port() {
  }
  
  @Override
  public void doExecute(CliContext ctx) throws InputException{
    CmdLine cmdLine = ctx.getCommandLine();
    Arg arg = cmdLine.assertNextArg(new String[]{ADD, DELETE, LIST});
    if(arg.toString().equals(ADD)){
      doAdd(ctx, cmdLine);
    }
    else if(arg.toString().equals(DELETE)){
      doDelete(ctx, cmdLine);
    }
    else{
      doList(ctx, cmdLine);
    }
  }
  
  void doAdd(CliContext ctx, CmdLine cmd) throws InputException{
    String name = cmd.assertOption(OPT_NAME, true).getValue();
    int min = cmd.assertOption(OPT_MIN, true).asInt();
    int max = cmd.assertOption(OPT_MAX, true).asInt();
    try{
      ctx.getCorus().getPortManagementFacade().addPortRange(name, min, max, getClusterInfo(ctx));
    }catch(PortRangeConflictException e){
      ctx.getConsole().println(e.getMessage());
    }catch(PortRangeInvalidException e){
      ctx.getConsole().println(e.getMessage());
    }
  }
  
  void doDelete(CliContext ctx, CmdLine cmd) throws InputException{
    String name = cmd.assertOption(OPT_NAME, true).getValue();
    boolean force = false;
    if(cmd.containsOption(OPT_FORCE, false)){
      force = true;
    }
    try{
      ctx.getCorus().getPortManagementFacade().removePortRange(name, force, getClusterInfo(ctx));
    }catch(PortActiveException e){    
      ctx.getConsole().println(e.getMessage());
    }
  }  
  
  void doRelease(CliContext ctx, CmdLine cmd) throws InputException{
    String name = cmd.assertOption(OPT_NAME, true).getValue();
    ctx.getCorus().getPortManagementFacade().releasePortRange(name, getClusterInfo(ctx));
  }    
  
  void doList(CliContext ctx, CmdLine cmd) throws InputException{
    displayResults(ctx.getCorus().getPortManagementFacade().getPortRanges(getClusterInfo(ctx)), ctx);
  }
  
  private void displayResults(Results<List<PortRange>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<PortRange>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for(PortRange range:result.getData()){
        displayPorts(range, ctx);
      }
    }
  }

  private void displayPorts(PortRange range, CliContext ctx) {
    Table   rangeTable;
    Row     row;

    rangeTable = new Table(ctx.getConsole().out(), 4, 20);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_NAME).setWidth(10);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_RANGE).setWidth(15);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_AVAIL).setWidth(24);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_ACTIVE).setWidth(24);
    
    rangeTable.drawLine('-', 0, 80);

    row = rangeTable.newRow();
    row.getCellAt(COL_NAME).append(range.getName());
    row.getCellAt(COL_RANGE).append(
      new StringBuffer("[")
        .append(range.getMin())
        .append(" - ")
        .append(range.getMax())
        .append("]").toString());
    row.getCellAt(COL_AVAIL).append(range.getAvailable().toString());
    row.getCellAt(COL_ACTIVE).append(range.getActive().toString());
    row.flush();
  }

  private void displayHeader(ServerAddress addr, CliContext ctx) {
    Table   rangeTable;
    Row     row;
    Row     headers;

    rangeTable = new Table(ctx.getConsole().out(), 1, 78);
    rangeTable.drawLine('=');
    row = rangeTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    rangeTable.drawLine(' ');

    rangeTable = new Table(ctx.getConsole().out(), 4, 20);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_NAME).setWidth(10);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_RANGE).setWidth(15);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_AVAIL).setWidth(24);
    rangeTable.getTableMetaData().getColumnMetaDataAt(COL_ACTIVE).setWidth(24);

    headers = rangeTable.newRow();

    headers.getCellAt(COL_NAME).append("Name");
    headers.getCellAt(COL_RANGE).append("Range");
    headers.getCellAt(COL_AVAIL).append("Available");
    headers.getCellAt(COL_ACTIVE).append("Active");

    headers.flush();
  }  
  
}
