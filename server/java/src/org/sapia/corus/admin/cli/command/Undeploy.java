package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.admin.cli.CliContext;


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
    ctx.getCorus().undeployExecConfig(name, getClusterInfo(ctx));
  }
  
  private void doUndeployDist(CliContext ctx) throws AbortException, InputException {
    try {
      String  dist    = null;
      String  version = null;
      CmdLine cmd     = ctx.getCommandLine();

      dist    = cmd.assertOption(super.DIST_OPT, true).getValue();
      version = cmd.assertOption(super.VERSION_OPT, true).getValue();

      super.displayProgress(ctx.getCorus().undeploy(dist, version,
                                                    getClusterInfo(ctx)),
                            ctx.getConsole());
    } catch (InputException e) {
      throw new InputException("File name expected as argument");
    }
  }
}
