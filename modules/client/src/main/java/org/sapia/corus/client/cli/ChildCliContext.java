package org.sapia.corus.client.cli;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.sort.SortSwitchInfo;

/**
 * Implements a context that is used when a command line is spawned from another
 * one.
 * 
 * @author yduchesne
 * 
 */
public class ChildCliContext extends Context implements CliContext {

  private CliContext             parent;
  private CmdLine                childCmd;
  private boolean                abortOnError;
  private StrLookupState         vars;
  private Stack<AutoClusterFlag> autoCluster = new Stack<>();

  public ChildCliContext(CliContext parent, CmdLine childCmd, StrLookupState vars) {
    this.parent   = parent;
    this.childCmd = childCmd;
    this.vars     = vars;
  }

  @Override
  public StrLookupState getVars() {
    return vars;
  }
  
  @Override
  public void addVars(Map<String, String> vars) {
    this.vars.set(CompositeStrLookup.newInstance()
          .add(StrLookup.mapLookup(vars))
          .add(this.vars.get()));
  }

  @Override
  public CmdLine getCommandLine() {
    return childCmd;
  }

  @Override
  public Console getConsole() {
    return parent.getConsole();
  }

  @Override
  public ClientFileSystem getFileSystem() {
    return parent.getFileSystem();
  }

  @Override
  public CorusConnector getCorus() {
    return parent.getCorus();
  }

  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aCause);
  }

  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aDescription, aCause);
  }

  @Override
  public List<CliError> getErrors() {
    return parent.getErrors();
  }

  @Override
  public int removeAllErrors() {
    return parent.removeAllErrors();
  }

  @Override
  public boolean isAbordOnError() {
    return abortOnError;
  }

  @Override
  public void setAbortOnError(boolean abortOnError) {
    this.abortOnError = abortOnError;
  }
  
  @Override
  public SortSwitchInfo[] getSortSwitches() {
    return parent.getSortSwitches();
  }
  
  @Override
  public void setSortSwitches(SortSwitchInfo[] sortSwitches) {
    parent.setSortSwitches(sortSwitches);
  }
  
  @Override
  public void unsetSortSwitches() {
    parent.unsetSortSwitches();
  }
  
  @Override
  public void setAutoClusterInfo(AutoClusterFlag flag) {
    autoCluster.push(flag);
  }
  
  @Override
  public OptionalValue<AutoClusterFlag> getAutoClusterInfo() {
    OptionalValue<AutoClusterFlag> toReturn = null;
    
    if (autoCluster.isEmpty()) {
      toReturn = OptionalValue.of(null);
    } else {
      toReturn = OptionalValue.of(autoCluster.peek());
    }
    
    return toReturn;
  }
  
  @Override
  public void unsetAutoClusterInfo() {
    if (!autoCluster.isEmpty()) {
      autoCluster.pop();
    }
  }
}
