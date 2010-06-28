package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;


/**
 * @author Yanick Duchesne
 */
public class Undeploy extends CorusCliCommand {
  
  public static final String OPT_EXEC_CONFIG = "e";
  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, true)){
      doUndeployExecConfig(ctx);
    }
    else{
      doUndeployDist(ctx);
    }
  }
  
  private void doUndeployExecConfig(CliContext ctx) throws AbortException, InputException {
    String name = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG, true).getValue();
    ctx.getCorus().getProcessorFacade().undeployExecConfig(name, getClusterInfo(ctx));
  }
  
  private void doUndeployDist(CliContext ctx) throws AbortException, InputException {
    try {
      String  dist    = null;
      String  version = null;
      CmdLine cmd     = ctx.getCommandLine();

      dist    = cmd.assertOption(DIST_OPT, true).getValue();
      version = cmd.assertOption(VERSION_OPT, true).getValue();

      super.displayProgress(ctx.getCorus().getDeployerFacade().undeploy(dist, version,
                                                    getClusterInfo(ctx)),
                            ctx.getConsole());
    } catch (RunningProcessesException e){
      throw new InputException(e.getMessage());
    } catch (InputException e) {
      throw new InputException("File name expected as argument");
    }
  }
}
