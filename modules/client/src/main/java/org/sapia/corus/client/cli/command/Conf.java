package org.sapia.corus.client.cli.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.ubik.net.ServerAddress;

public class Conf extends CorusCliCommand{
  
  public static final String ARG_ADD = "add";
  public static final String ARG_DEL = "del";
  public static final String ARG_LS  = "ls";
  
  public static final String OPT_PROPERTY = "p";
  public static final String OPT_TAG = "t";
  public static final String OPT_SCOPE = "s";
  public static final String OPT_SCOPE_SVR = "s";
  public static final String OPT_SCOPE_PROC = "p";

  private static final int COL_PROP_TAG   = 0;
  private static final int COL_PROP_NAME  = 0;
  private static final int COL_PROP_VALUE = 1;
  
  public enum Op{
    ADD,
    DELETE,
    LIST;
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException{
    
    Op op = null;
    if(ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()){
      String opArg = ctx.getCommandLine().assertNextArg().getName();
      if(opArg.equalsIgnoreCase(ARG_ADD)){
        op = Op.ADD;
      }
      else if(opArg.equalsIgnoreCase(ARG_DEL)){
        op = Op.DELETE;
      }
      else if(opArg.equalsIgnoreCase(ARG_LS)){
        op = Op.LIST;
      }
      else{
        throw new InputException("Unknown argument " + opArg + "; expecting one of: add | del | ls");
      }
    }
    else{
      throw new InputException("Missing argument; expecting one of: add | del | ls");
    }

    if(op != null){
      if(ctx.getCommandLine().containsOption(OPT_TAG, false)){
        handleTag(op, ctx);
      }
      else if(ctx.getCommandLine().containsOption(OPT_PROPERTY, false) || 
              ctx.getCommandLine().containsOption(OPT_SCOPE_PROC, true)){
        handlePropertyOp(op, ctx);
      }
      else{
        throw new InputException("Missing options (-t or -p)");
      }
    }
  }
  
  private void handleTag(Op op, CliContext ctx) throws InputException{
    if(op == Op.ADD){
      String[] toAdd = ctx.getCommandLine().assertOption(OPT_TAG, true)
        .getValue().split(",");
      Set<String> set = new HashSet<String>();
      for(int i = 0; i < toAdd.length; i++){
        set.add(toAdd[i].trim());
      }
      
      ctx.getCorus().getConfigFacade().addTags(set, getClusterInfo(ctx));
    }
    else if(op == Op.DELETE){
      String toRemove = ctx.getCommandLine().assertOption(OPT_TAG, true).getValue();
      ctx.getCorus().getConfigFacade().removeTag(toRemove, getClusterInfo(ctx));
    }
    else if(op == Op.LIST){
      displayTagResults(ctx.getCorus().getConfigFacade().getTags(getClusterInfo(ctx)), ctx);
    }
  }
  
  private void handlePropertyOp(Op op, CliContext ctx) throws InputException{ 
    PropertyScope scope = PropertyScope.SERVER;
    if(ctx.getCommandLine().containsOption(OPT_SCOPE, true)){
      String scopeOpt = ctx.getCommandLine().assertOption(OPT_SCOPE, true).getValue();
      if(scopeOpt.startsWith(OPT_SCOPE_PROC)){
        scope = PropertyScope.PROCESS;
      }
      else if(scopeOpt.startsWith(OPT_SCOPE_SVR)){
        scope = PropertyScope.SERVER;
      }
      else{
        throw new InputException("Scope (-s) not valid, expecting s[vr] or p[roc]");
      }
    }
    if(op == Op.ADD){
      String pair = ctx.getCommandLine().assertOption(OPT_PROPERTY, true).getValue();
      String[] nameValue = pair.split("=");
      if(nameValue.length != 2){
        throw new InputException("Invalid property format; expected: <name>=<value>");
      }
      else{
        ctx.getCorus().getConfigFacade().addProperty(scope, nameValue[0], nameValue[1], getClusterInfo(ctx));
      }
    }
    else if(op == Op.DELETE){
      String name = ctx.getCommandLine().assertOption(OPT_PROPERTY, true).getValue();
      ctx.getCorus().getConfigFacade().removeProperty(scope, name, getClusterInfo(ctx));
    }
    else if(op == Op.LIST){
      Results<List<NameValuePair>> results = ctx.getCorus().getConfigFacade().getProperties(scope, getClusterInfo(ctx));
      displayPropertyResults(results, ctx);
    }
    
  }
  
  private void displayTagResults(Results<Set<String>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<Set<String>> result = res.next();
      displayTagsHeader(result.getOrigin(), ctx);
      for(String tag:result.getData()){
        displayTag(tag, ctx);
      }
    }
  }
  
  private void displayTagsHeader(ServerAddress addr, CliContext ctx) {
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

    distTable = new Table(ctx.getConsole().out(), 1, 78);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_TAG).setWidth(78);
    
    headers = distTable.newRow();

    headers.getCellAt(COL_PROP_TAG).append("Tag");
    headers.flush();
  }
  
  private void displayTag(String tag, CliContext ctx) {
    Table         distTable = new Table(ctx.getConsole().out(), 1, 78);
    Row           row;
    
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_TAG).setWidth(78);

    distTable.drawLine('-');

    row = distTable.newRow();
    row.getCellAt(COL_PROP_TAG).append(tag);
    row.flush();
  }  
  
  private void displayPropertyResults(Results<List<NameValuePair>> res, CliContext ctx) {
    while (res.hasNext()) {
      Result<List<NameValuePair>> result = res.next();
      displayPropertiesHeader(result.getOrigin(), ctx);
      for(NameValuePair props:result.getData()){
        displayProperties(props, ctx);
      }
    }
  }
  
  private void displayPropertiesHeader(ServerAddress addr, CliContext ctx) {
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

    distTable = new Table(ctx.getConsole().out(), 2, 35);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_NAME).setWidth(40);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_VALUE).setWidth(30);

    headers = distTable.newRow();

    headers.getCellAt(COL_PROP_NAME).append("Name");
    headers.getCellAt(COL_PROP_VALUE).append("Value");
    headers.flush();
  }
  
  private void displayProperties(NameValuePair prop, CliContext ctx) {
    Table         distTable = new Table(ctx.getConsole().out(), 2, 35);
    Row           row;
    
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_NAME).setWidth(40);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PROP_VALUE).setWidth(30);

    distTable.drawLine('-');

    row = distTable.newRow();
    row.getCellAt(COL_PROP_NAME).append(prop.getName());
    row.getCellAt(COL_PROP_VALUE).append(prop.getValue());
    row.flush();
  }
  
}
