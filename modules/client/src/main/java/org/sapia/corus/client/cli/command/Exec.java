package org.sapia.corus.client.cli.command;

import java.io.IOException;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Executes processes.
 * 
 * @author Yanick Duchesne
 */
public class Exec extends CorusCliCommand {

  public static final String OPT_EXEC_CONFIG = "e";
  public static final String OPT_SCRIPT      = "s";
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, true)) {
      doExecuteConfig(ctx);
    } else if(ctx.getCommandLine().containsOption(OPT_SCRIPT, true)) {
      doExecuteScript(ctx);
    } else {
      doExecuteProcesses(ctx);
    }
  }

  private void doExecuteConfig(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String configName = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG, true).getValue();
    displayProgress(ctx.getCorus().getProcessorFacade().execConfig(configName, cluster), ctx);
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
  
  private void doExecuteProcesses(CliContext ctx) throws AbortException, InputException {
    String  dist      = null;
    String  version   = null;
    String  profile   = null;
    String  vmName    = null;
    int     instances = 1;
    CmdLine cmd       = ctx.getCommandLine();

    dist = cmd.assertOption(DIST_OPT, true).getValue();

    version = cmd.assertOption(VERSION_OPT, true).getValue();

    profile = cmd.assertOption(PROFILE_OPT, true).getValue();

    if (cmd.containsOption(VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(VM_INSTANCES, true)) {
      instances = cmd.assertOption(VM_INSTANCES, true).asInt();
    }

    ClusterInfo cluster = getClusterInfo(ctx);
    ProcessCriteria criteria = ProcessCriteria.builder()
      .name(vmName)
      .distribution(dist)
      .version(version)
      .profile(profile)
      .build();
    try{
      displayProgress(
              ctx.getCorus().getProcessorFacade().exec(criteria, instances, cluster),
              ctx
      );
    }catch(TooManyProcessInstanceException e){
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }
  
}
