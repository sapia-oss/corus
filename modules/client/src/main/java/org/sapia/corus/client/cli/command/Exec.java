package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;


/**
 * @author Yanick Duchesne
 */
public class Exec extends CorusCliCommand {

  public static final String OPT_EXEC_CONFIG = "e";
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, true)){
      doExecuteConfig(ctx);
    }
    else{
      doExecuteProcesses(ctx);
    }
    
  }

  private void doExecuteConfig(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String configName = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG, true).getValue();
    displayProgress(ctx.getCorus().getProcessorFacade().exec(configName, cluster), ctx);
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

    if (vmName != null) {
      displayProgress(
              ctx.getCorus().getProcessorFacade().exec(
                      dist, version, profile, vmName, instances, cluster),
              ctx);
    } else {
      displayProgress(
              ctx.getCorus().getProcessorFacade().exec(
                      dist, version, profile, instances, cluster),
              ctx);
    }
  }
  
}
