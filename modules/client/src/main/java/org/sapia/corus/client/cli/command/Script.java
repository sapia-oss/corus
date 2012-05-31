package org.sapia.corus.client.cli.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.ChildCliContext;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.CorusCommandFactory;

public class Script extends CorusCliCommand{

  private static final String COMMENT_MARKER = "#";
  
  private static CorusCommandFactory commands = new CorusCommandFactory();
  
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
    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to execute script", e);
      ctx.getConsole().println(err.getSimpleMessage());      
    }
  }
    
  public static void processScript(File scriptFile, CliContext ctx) throws IOException, CommandNotFoundException, Exception{
    if (scriptFile.exists()) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile)));
        String line;
        while((line = reader.readLine()) != null){
          if(!line.startsWith(COMMENT_MARKER)){
            CmdLine cmdLine = CmdLine.parse(line);
            if(cmdLine.hasNext()){
              ChildCliContext cmdContext = new ChildCliContext(ctx, cmdLine);
              String commandName = cmdLine.assertNextArg().getName();
              ctx.getConsole().println("["+commandName+"]");
              CorusCliCommand cmd = (CorusCliCommand)commands.getCommandFor(commandName);
              cmd.execute(cmdContext);
            }
          }
        }
      } finally {
        try {
          if(reader != null) reader.close();
        } catch (Exception e2) {
          // TODO: handle exception
        }
      }
      
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }
  
}
