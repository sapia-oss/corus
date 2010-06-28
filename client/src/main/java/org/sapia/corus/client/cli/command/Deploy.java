package org.sapia.corus.client.cli.command;

import java.io.File;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdElement;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;


/**
 * @author Yanick Duchesne
 */
public class Deploy extends CorusCliCommand {
  
  public static final String OPT_EXEC_CONF = "e";

  @Override
  protected void doExecute(CliContext ctx)
  throws AbortException, InputException {
    
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
  
  private void deployDistribution(CliContext ctx, String fileName)
  throws AbortException, InputException {
    if(fileName.endsWith("xml")){
      deployExec(ctx, fileName);
    }
    else{
      try {
        displayProgress(ctx.getCorus().getDeployerFacade().deploy(fileName,
          getClusterInfo(ctx)),
          ctx.getConsole());
      } catch (ConcurrentDeploymentException e) {
        ctx.getConsole().println("Distribution file already being deployed");
      } catch (Exception e) {
        ctx.getConsole().println("Problem deploying distribution");
        e.printStackTrace(ctx.getConsole().out());
      }
    }
  }
  
  private void deployExec(CliContext ctx, String fileName) 
  throws AbortException, InputException {
    File file = new File(fileName);
    if(!file.exists()){
      ctx.getConsole().println("File not found: " + fileName);
    }
    else if(file.isDirectory()){
      ctx.getConsole().println("Resource is a directory: " + fileName);
    }
    else{
      try{
        ctx.getCorus().getProcessorFacade().deployExecConfig(fileName, getClusterInfo(ctx));
      }catch(Exception e){
        ctx.getConsole().println("Could not deploy execution configuration");
        e.printStackTrace(ctx.getConsole().out());
      }
    }
  }
}
