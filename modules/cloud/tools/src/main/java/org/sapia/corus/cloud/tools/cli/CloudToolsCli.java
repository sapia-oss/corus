package org.sapia.corus.cloud.tools.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.ConsoleInputFactory;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.console.InputException;
import org.sapia.corus.cloud.platform.cli.CliModule;
import org.sapia.corus.cloud.platform.cli.CliModule.StatusCode;
import org.sapia.corus.cloud.platform.cli.CliModuleContext;
import org.sapia.corus.cloud.platform.cli.CliModuleLoader;
import org.sapia.corus.cloud.platform.workflow.WorkflowDiagnosticsHelper;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog.Level;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult.Outcome;

public class CloudToolsCli {
  
  private Console console;
  
  public CloudToolsCli(Console console) {
    this.console        = console;
  }
  
    
  public static void main(String[] args) {
    CmdLine cmd = CmdLine.parse(args);
    Console console = new Console(ConsoleInputFactory.createDefaultConsoleInput(), DefaultConsoleOutput.newInstance());
    
    CloudToolsCli cli = new CloudToolsCli(console);
    cli.interact(cmd);
  } 
 
  /**
   * @param cmd the {@link CmdLine} to process.
   * @return the status code resulting from this given command's execution.
   */
  public int interact(CmdLine cmd) {
    try {
      return doInteract(cmd);
    } catch (InputException e) {
      console.println(e.getMessage());
      return StatusCode.FAILURE.value();
    } catch (RuntimeException e) {
      console.println(e.getMessage());
      console.printStackTrace(e);
      return StatusCode.FAILURE.value();
    }
  }
 
  public int doInteract(CmdLine cmd) {
    
    if (cmd.isEmpty()) {
      console.println("Expected: <provider> <command> [<options>]");
      return StatusCode.FAILURE.value();
    } else if (cmd.size() == 1 && cmd.first().nameEquals("help","-help", "h", "-h")) {
      console.println("Expected: <provider> <command> [<options>]");
      return StatusCode.SUCCESS.value();
    } else if (cmd.size() < 2) {
      invalidSyntax(cmd);
      return StatusCode.FAILURE.value();
    } else {
      String provider;
      String commandName;
      if (cmd.hasNext() && cmd.isNextArg()) {
        provider = cmd.assertNextArg().getName();
      } else {
        invalidSyntax(cmd);
        return StatusCode.FAILURE.value();
      }
      if (cmd.hasNext() && cmd.isNextArg()) {
        commandName = cmd.assertNextArg().getName();
      } else {
        invalidSyntax(cmd);
        return StatusCode.FAILURE.value();
      }
      Level logLevel = Level.INFO;
      if (cmd.containsOption("v")) {
        logLevel = Level.forName(cmd.getOptNotNull("v").getValue());
        cmd.removeOption("v");
      }
      CliModule module = CliModuleLoader.getInstance().load(provider, commandName);

      if (cmd.hasNext() && cmd.peek().nameEquals("help")) {
        console.repeat('=');
        console.println(provider + " " + commandName);
        console.repeat('-');
        module.displayHelp(new CliModuleContext(logLevel, console));
        return StatusCode.SUCCESS.value();
      } else {
        WorkflowResult result = module.interact(new CliModuleContext(logLevel, console), cmd);
        new WorkflowDiagnosticsHelper().displayDiagnostics(result);
        if (result.getOutcome() == Outcome.SUCCESS) {
          return StatusCode.SUCCESS.value();
        } else {
          return StatusCode.FAILURE.value();
        }
      }
    }
  }
  
  private void invalidSyntax(CmdLine invalid) {
    console.println("Invalid syntax " + invalid.toString());
    console.println("Expected <provider> <command> [<options>]");    
  }
}
