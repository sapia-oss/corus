package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
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
public class Resume extends CorusCliCommand {
  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    displayProgress(ctx.getCorus().restart(getClusterInfo(ctx)),
                    ctx.getConsole());
  }
}
