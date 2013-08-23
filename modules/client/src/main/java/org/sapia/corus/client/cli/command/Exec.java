package org.sapia.corus.client.cli.command;

import java.io.IOException;

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

  public static final String OPT_EXEC_CONFIG = "e";
  public static final String OPT_SCRIPT      = "s";
  public static final int  DEFAULT_EXEC_WAIT_TIME_SECONDS = 120;  
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, true)) {
      new ExecProcessesByConfig().execute((Context) ctx); 
    } else if(ctx.getCommandLine().containsOption(OPT_SCRIPT, true)) {
      doExecuteScript(ctx);
    } else {
      new ExecProcessByDescriptors().execute((Context) ctx);      
    }
  }
  
  private void doExecuteScript(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String alias = ctx.getCommandLine().assertOption(OPT_SCRIPT, true).getValue();
    try {
      displayProgress(ctx.getCorus().getScriptManagementFacade().execScript(alias, cluster), ctx);
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be executed: " + alias, e);
    } catch (ScriptNotFoundException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be found for alias: " + alias, e);
    }
  }  
  
}
