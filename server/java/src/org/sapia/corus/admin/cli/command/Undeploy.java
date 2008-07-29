package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

import org.sapia.corus.admin.cli.CliContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Undeploy extends CorusCliCommand {
  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
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
