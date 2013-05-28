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
  
  private static final String SCRIPT_DESC_UNDEFINED = "no desc.";
  
  public static final String OPT_EXEC_CONF      = "e";
  public static final String OPT_FILE           = "f";
  public static final String OPT_SCRIPT         = "s";
  public static final String OPT_DESC_OR_DIR    = "d";
  public static final String OPT_ALIAS          = "a";

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONF, true)){
      deployExec(ctx, ctx.getCommandLine().assertOption(OPT_EXEC_CONF, true).getValue());
    } else if(ctx.getCommandLine().containsOption(OPT_FILE, true)){
      deployFile(ctx, ctx.getCommandLine().assertOption(OPT_FILE, true).getValue());
    } else if(ctx.getCommandLine().containsOption(OPT_SCRIPT, true)){
      deployScript(
          ctx, 
          ctx.getCommandLine().assertOption(OPT_SCRIPT, true).getValue(),
          ctx.getCommandLine().assertOption(OPT_ALIAS, true).getValue());
    } else {
      if(ctx.getCommandLine().isNextArg()){
        while(ctx.getCommandLine().hasNext()){
          CmdElement elem = ctx.getCommandLine().next();
          if(elem instanceof Arg){
            deployDistribution(ctx, elem.getName());
          }
        }
      } else{
        throw new InputException("File name expected as argument");
      }
    }
  }
  
  private void deployDistribution(CliContext ctx, String fileName) throws AbortException, InputException {
    if (fileName.endsWith("xml")) {
      deployExec(ctx, fileName);
    }
    else {
      try {
        displayProgress(
                ctx.getCorus().getDeployerFacade().deployDistribution(
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
  
  private void deployScript(CliContext ctx, String fileName, String alias) throws AbortException, InputException {
    String desc = null;
    if (ctx.getCommandLine().containsOption(OPT_DESC_OR_DIR, true)) {
      desc = ctx.getCommandLine().assertOption(OPT_DESC_OR_DIR, true).getValue();
    } else {
      desc = SCRIPT_DESC_UNDEFINED;
    }  
    try {
      displayProgress(
              ctx.getCorus().getDeployerFacade().deployScript(
                      fileName,
                      alias,
                      desc,
                      getClusterInfo(ctx)),
                      ctx);
      
    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem deploying script", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }  
  
  private void deployFile(CliContext ctx, String fileName) throws AbortException, InputException {
    String destDir = null;
    if (ctx.getCommandLine().containsOption(OPT_DESC_OR_DIR, true)) {
      destDir = ctx.getCommandLine().assertOption(OPT_DESC_OR_DIR, true).getValue();
    }   
    try {
      displayProgress(
              ctx.getCorus().getDeployerFacade().deployFile(
                      fileName,
                      destDir,
                      getClusterInfo(ctx)),
                      ctx);
      
    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Problem deploying file", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }  
  
  private void deployExec(CliContext ctx, String fileName) 
  throws AbortException, InputException {
    File file = ctx.getFileSystem().getFile(fileName);
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
