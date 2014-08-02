package org.sapia.corus.client.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.exec.ExecProcessByDescriptors;
import org.sapia.corus.client.cli.command.exec.ExecProcessesByConfig;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;

/**
 * Executes processes.
 * 
 * @author Yanick Duchesne
 */
public class Exec extends AbstractExecCommand {

  public static final OptionDef OPT_EXEC_CONFIG = new OptionDef("e", true);
  public static final OptionDef OPT_SCRIPT      = new OptionDef("s", true);
  private static final List<OptionDef> ADDITIONAL_OPTIONS = new ArrayList<OptionDef>(AVAIL_OPTIONS);
  static {
    ADDITIONAL_OPTIONS.add(OPT_EXEC_CONFIG);
    ADDITIONAL_OPTIONS.add(OPT_SCRIPT);
  }
 
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    if (ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG.getName(), true)) {
      new ExecProcessesByConfig().execute((Context) ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_SCRIPT.getName(), true)) {
      doExecuteScript(ctx);
    } else {
      new ExecProcessByDescriptors().execute((Context) ctx);
    }
  }
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return ADDITIONAL_OPTIONS;
  }

  private void doExecuteScript(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String alias = ctx.getCommandLine().assertOption(OPT_SCRIPT.getName(), true).getValue();
    try {
      displayProgress(ctx.getCorus().getScriptManagementFacade().execScript(alias, cluster), ctx);
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be executed: " + alias, e);
    } catch (ScriptNotFoundException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be found for alias: " + alias, e);
    }
  }

}
