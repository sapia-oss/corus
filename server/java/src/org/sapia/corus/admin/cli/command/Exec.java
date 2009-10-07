package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.cli.CliContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Exec extends CorusCliCommand {

  public static final String OPT_EXEC_CONFIG = "e";
  
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
    displayProgress(ctx.getCorus().exec(configName, cluster), ctx.getConsole());
  } 
  private void doExecuteProcesses(CliContext ctx) throws AbortException, InputException {
    String  dist      = null;
    String  version   = null;
    String  profile   = null;
    String  vmName    = null;
    int     instances = 1;
    CmdLine cmd       = ctx.getCommandLine();

    dist = cmd.assertOption(super.DIST_OPT, true).getValue();

    version = cmd.assertOption(super.VERSION_OPT, true).getValue();

    profile = cmd.assertOption(super.PROFILE_OPT, true).getValue();

    if (cmd.containsOption(super.VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(super.VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(super.VM_INSTANCES, true)) {
      instances = cmd.assertOption(super.VM_INSTANCES, true).asInt();
    }

    ClusterInfo cluster = getClusterInfo(ctx);

    if (vmName != null) {
      displayProgress(ctx.getCorus().exec(dist, version, profile, vmName,
                                           instances, cluster), ctx.getConsole());
    } else {
      displayProgress(ctx.getCorus().exec(dist, version, profile, instances,
                                           cluster), ctx.getConsole());
    }
  }
}
