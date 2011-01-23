package org.sapia.corus.client.cli.command;

import java.io.File;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdElement;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;


/**
 * @author Yanick Duchesne
 */
public class Deploy extends CorusCliCommand {
  
  public static final String OPT_EXEC_CONF = "e";

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    if(ctx.getCommandLine().isNextArg()){
      while(ctx.getCommandLine().hasNext()){
        CmdElement elem = ctx.getCommandLine().next();
        if(elem instanceof Arg){
          deployDistribution(ctx, elem.getName());
        }
      }
    }
    else if(ctx.getCommandLine().containsOption(OPT_EXEC_CONF, true)){
      deployExec(ctx, ctx.getCommandLine().assertOption(OPT_EXEC_CONF, true).getValue());
    }
    else{
      throw new InputException("File name expected as argument");
    }
  }
  
  private void deployDistribution(CliContext ctx, String fileName) throws AbortException, InputException {
    if (fileName.endsWith("xml")) {
      deployExec(ctx, fileName);
    }
    else {
      try {
        displayProgress(
                ctx.getCorus().getDeployerFacade().deploy(
                        fileName,
                        getClusterInfo(ctx)),
                        ctx);
        
      } catch (ConcurrentDeploymentException e) {
        CliError err = ctx.createAndAddErrorFor(this, "Distribution file already being deployed", e);
        ctx.getConsole().println(err.getSimpleMessage());

      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Problem deploying distribution", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
  }
  
  private void deployExec(CliContext ctx, String fileName) 
  throws AbortException, InputException {
    File file = new File(fileName);
    if (!file.exists()) {
      CliError err = ctx.createAndAddErrorFor(this, new DeploymentException("File not found: " + fileName));
      ctx.getConsole().println(err.getSimpleMessage());
      
    } else if (file.isDirectory()) {
      CliError err = ctx.createAndAddErrorFor(this, new DeploymentException("Resource is a directory: " + fileName));
      ctx.getConsole().println(err.getSimpleMessage());
      
    } else {
      try {
        ctx.getCorus().getProcessorFacade().deployExecConfig(fileName, getClusterInfo(ctx));
      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Could not deploy execution configuration", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
  }
  
}
