package org.sapia.corus.admin.cli.command;

import java.io.File;

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
  
  public static final String OPT_EXEC_CONF = "e";

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
    
    if(ctx.getCommandLine().isNextArg()){
      deployDistribution(ctx);
    }
    else if(ctx.getCommandLine().containsOption(OPT_EXEC_CONF, true)){
      deployExec(ctx);
    }
    else{
      throw new InputException("File name expected as argument");
    }
  }
  
  private void deployDistribution(CliContext ctx)
  throws AbortException, InputException {
    String fileName = null;
    try {
      fileName = ctx.getCommandLine().assertNextArg().getName();
      displayProgress(ctx.getCorus().deploy(fileName,
        getClusterInfo(ctx)),
        ctx.getConsole());
    } catch (ConcurrentDeploymentException e) {
      ctx.getConsole().println("Distribution file already being deployed");
    } catch (Exception e) {
      ctx.getConsole().println("Problem deploying distribution");
      e.printStackTrace(ctx.getConsole().out());
    }
  }
  
  private void deployExec(CliContext ctx) 
  throws AbortException, InputException {
    String fileName = null;
    try {
      fileName = ctx.getCommandLine().assertOption(OPT_EXEC_CONF, true).getValue();
    } catch (InputException e) {
      
      throw new InputException("File name expected as argument");
    } 

    File file = new File(fileName);
    if(!file.exists()){
      ctx.getConsole().println("File not found: " + fileName);
    }
    else if(file.isDirectory()){
      ctx.getConsole().println("Resource is a directory: " + fileName);
    }
    else{
      try{
        ctx.getCorus().deployExecConfig(fileName, getClusterInfo(ctx));
      }catch(Exception e){
        ctx.getConsole().println("Could not deploy execution configuration");
        e.printStackTrace(ctx.getConsole().out());
      }
    }
  }
}
