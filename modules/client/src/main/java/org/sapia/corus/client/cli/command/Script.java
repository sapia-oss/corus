package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.sapia.console.AbortException;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.Interpreter;

public class Script extends CorusCliCommand{

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    if (!ctx.getCommandLine().hasNext()) {
      throw new InputException("Path to script file expected");
    }
   
    try {
      File scriptFile = new File(ctx.getCommandLine().next().getName());
      processScript(scriptFile, ctx);
    } catch (FileNotFoundException e) {
      throw new InputException(e.getMessage());
    } catch (Throwable e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to execute script", e);
      ctx.getConsole().println(err.getSimpleMessage());      
    }
  }
    
  public static void processScript(File scriptFile, CliContext ctx) throws IOException, CommandNotFoundException, Throwable {
    if (scriptFile.exists()) {
      Interpreter interpreter = new Interpreter(ctx.getCorus());
      interpreter.interpret(new FileReader(scriptFile), new HashMap<String, String>());
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }
  
}
