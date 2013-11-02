package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.CorusScript;
import org.sapia.corus.client.cli.Interpreter;

/**
 * Interprets a Corus script specified at the command-line.
 *  
 * @author yduchesne
 *
 */
public class Script extends CorusCliCommand {
  
  private static final String ENGINE_OPT   = "e";
  private static final String INCLUDES_OPT = "i";
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    Arg    fileName;
    String scriptEngineName = null;
    if (ctx.getCommandLine().containsOption(ENGINE_OPT, true)) {
      scriptEngineName = ctx.getCommandLine().assertOption(ENGINE_OPT, true).getValue();
    }
    fileName = getFirstArg(ctx);
    if (fileName == null) {
      throw new InputException("Path to script file expected");
    }
   
    try {
      File scriptFile = ctx.getFileSystem().getFile(fileName.getName());
      processScript(scriptFile, scriptEngineName, ctx);
    } catch (FileNotFoundException e) {
      throw new InputException(e.getMessage());
    } catch (Throwable e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to execute script", e);
      ctx.getConsole().println(err.getSimpleMessage());      
    }
  }
    
  public void processScript(File scriptFile, String scriptEngineName, CliContext ctx) throws IOException, CommandNotFoundException, Throwable {
    if (scriptFile.exists()) {
      Interpreter interpreter = new Interpreter(ctx.getCorus());
      if (scriptEngineName == null) {
        interpreter.interpret(new FileReader(scriptFile), ctx.getVars());
      } else {
        String[] includes = getOpt(ctx, INCLUDES_OPT, "").getValue().split(";");
        CorusScript scriptRunner = new CorusScript(interpreter, includes, scriptEngineName);
        scriptRunner.runScript(scriptFile.getAbsolutePath(), new HashMap<String, Object>());
      }
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }
  
}
