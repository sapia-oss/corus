package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;

import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.deployer.ConcurrentDeploymentException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Deploy extends CorusCliCommand {
  /**
   * Constructor for Deploy.
   */
  public Deploy() {
    super();
  }
  
  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
  throws AbortException, InputException {
    try {
      String fileName = ctx.getCommandLine().assertNextArg().getName();
      displayProgress(ctx.getCorus().deploy(fileName,
        getClusterInfo(ctx)),
        ctx.getConsole());
    } catch (InputException e) {
      throw new InputException("File name expected as argument");
    } catch (ConcurrentDeploymentException e) {
      ctx.getConsole().println("Distribution file already being deployed");
    } catch (Exception e) {
      ctx.getConsole().println("Problem deploying distribution");
      e.printStackTrace(ctx.getConsole().out());
    }
  }
}
