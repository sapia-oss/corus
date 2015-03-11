package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CorusConsoleOutput;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.CompositePattern;
import org.sapia.corus.client.facade.FacadeInvocationContext;

/**
 * Displays the count of items in the result of a command.
 * 
 * @author yduchesne
 *
 */
public class Match extends NoOptionCommand {
  
  private static final String APPLY = "apply";
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    CmdLine cmd = ctx.getCommandLine();
    
    if (!cmd.hasNext() || !cmd.isNextArg()) {
      throw new InputException("Expected 'apply <script_file_name>' or <pattern>, followed by <command>");
    }
    Arg    arg        = cmd.assertNextArg();
    String scriptName = null;
    Arg    patternArg;
    if (arg.getName().equals(APPLY)) {
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<script_file_name> expected after 'apply'");
      }
      scriptName = cmd.assertNextArg().getName();
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<pattern> expected after 'apply'");
      }
      patternArg = cmd.assertNextArg();
    } else {
      patternArg = arg;
    }
    CmdLine remaining = new CmdLine();
    while (cmd.hasNext()) {
      remaining.addElement(cmd.next());
    }
 
    if (remaining.size() == 0) {
      throw new InputException("Invalid input. Expected: 'apply <script_file_name>' or <pattern>, followed by <command> (<command> is missing), got: " + patternArg.getName());
    }
    
    CompositePattern pattern = CompositePattern.newInstance();
    String[] patternValues   = StringUtils.split(patternArg.getName(), ",");
    for (String p : patternValues) {
      pattern.add(new Matcheable.DefaultPattern(ArgMatchers.parse(p)));
    }
    String  toExecute        = remaining.toString();
    
    if (scriptName != null) {
      turnOffOutput(ctx);
    }
    try {
      ctx.getCorus().getContext().setResultFilter(pattern);
      Interpreter intp = new Interpreter(ctx.getConsole(), ctx.getCorus());
      intp.eval(toExecute, ctx.getVars());
    } catch (CommandNotFoundException e) {
      throw new InputException("Command not found: " + e.getMessage());
    } catch (InputException e) {
      throw e;
    } catch (Throwable e) {
      throw new AbortException("Could not execute command: " + toExecute, e);
    } finally {
      ctx.getCorus().getContext().unsetResultFilter();
      if (scriptName != null) {
        turnOnOutput(ctx);
      }
    }
    
    if (scriptName != null && FacadeInvocationContext.get() != null && FacadeInvocationContext.get() instanceof List) {
      List<?> results = (List<?>) FacadeInvocationContext.get();
      if (!results.isEmpty()) {
        ClusterInfo info = new ClusterInfo(true);
        for (Object r: results) {
          if (r instanceof Result) {
            info.addTarget(((Result<?>) r).getOrigin().getEndpoint().getServerAddress()); 
          }
        }
        
        File        scriptFile  = ctx.getFileSystem().getFile(scriptName);
        Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
        interpreter.setAutoCluster(info);
        try {
          interpreter.interpret(new FileReader(scriptFile), ctx.getVars());
        } catch (AbortException e) {
          throw e;
        } catch (CommandNotFoundException e) {
          throw new InputException("Command not found: " + e.getMessage());
        } catch (InputException e) {
          throw e;
        } catch (Throwable e) {
          throw new AbortException("Error executing script: " + scriptName, e);
        }
      }
    }
  }
  
  private void turnOffOutput(CliContext ctx) {
    if (ctx.getConsole().out() instanceof CorusConsoleOutput) {
      ((CorusConsoleOutput) ctx.getConsole().out()).turnOff();
    }
  }
  
  private void turnOnOutput(CliContext ctx) {
    if (ctx.getConsole().out() instanceof CorusConsoleOutput) {
      ((CorusConsoleOutput) ctx.getConsole().out()).turnOn();
    }
  }

}
