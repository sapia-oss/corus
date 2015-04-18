package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * Displays the count of items in the result of a command.
 * 
 * @author yduchesne
 *
 */
public class Match extends NoOptionCommand {
  
  private static final String APPLY   = "apply";
  private static final String COMMAND = "command";
  
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
    Arg    patternArg;
    Reader script     = null;
    if (arg.getName().equals(APPLY)) {
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<script_file_name> expected after 'apply'");
      }
      String scriptName = cmd.assertNextArg().getName();
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<pattern> expected after 'apply'");
      }
      patternArg = cmd.assertNextArg();
      
      File scriptFile  = ctx.getFileSystem().getFile(scriptName);
      if (!scriptFile.exists()) {
        throw new InputException("Script not found: " + scriptName);
      }
      
      try {
        script = new FileReader(scriptFile);
      } catch (IOException e) {
        throw new AbortException("Error occured trying to open script: " + scriptName, e);
      }
      
    } else if (arg.getName().equals(COMMAND)) {
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("\"<command_line>\" expected after 'apply'");
      }
      String command = cmd.assertNextArg().getName();
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<pattern> expected after 'apply'");
      }
      patternArg = cmd.assertNextArg();

      script = new StringReader(command.replace("'", "\""));
    } else {
      patternArg = arg;
    }
    
    try {
      CmdLine remaining = new CmdLine();
      while (cmd.hasNext()) {
        remaining.addElement(cmd.next());
      }
   
      if (remaining.size() == 0) {
        throw new InputException("Invalid input. Expected: 'apply <script_file_name>' or 'command \"<command_line>\"' or <pattern>, followed by <command> (<command> is missing), got: " + patternArg.getName());
      }
      
      try {
        doExecute(ctx, script, patternArg, remaining);
      } catch (IOException e) {
        throw new AbortException("I/O error executing command", e);
      }
      
    } finally {
      try {
        if (script != null) {
          script.close();
        }
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  public void doExecute(CliContext ctx, Reader script, Arg patternArg, CmdLine remaining) throws IOException, AbortException, InputException {
    CompositePattern pattern = CompositePattern.newInstance();
    String[] patternValues   = StringUtils.split(patternArg.getName(), ",");
    for (String p : patternValues) {
      pattern.add(new Matcheable.DefaultPattern(ArgMatchers.parse(p)));
    }
    String  toExecute        = remaining.toString();
    
    if (script != null) {
      turnOffOutput(ctx);
    }
    try {
      ctx.getCorus().getContext().setResultFilter(pattern);
      Interpreter intp = new Interpreter(ctx.getConsole(), ctx.getCorus());
      if (ctx.getAutoClusterInfo().isSet()) {
        intp.setAutoClusterInfo(ctx.getAutoClusterInfo().get());
      }
      intp.eval(toExecute, ctx.getVars());
    } catch (CommandNotFoundException e) {
      throw new InputException("Command not found: " + e.getMessage());
    } catch (InputException e) {
      throw e;
    } catch (Throwable e) {
      throw new AbortException("Could not execute command: " + toExecute, e);
    } finally {
      ctx.getCorus().getContext().unsetResultFilter();
      
      if (script != null) {
        turnOnOutput(ctx);
      }
    }
    
    if (script != null && FacadeInvocationContext.get() != null && FacadeInvocationContext.get() instanceof List) {
      List<?> results = (List<?>) FacadeInvocationContext.get();
      if (!results.isEmpty()) {
        ClusterInfo info = new ClusterInfo(true);
        List<CorusHost> selectedHosts = new ArrayList<CorusHost>();
        for (Object r: results) {
          if (r instanceof Result) {
            info.addTarget(((Result<?>) r).getOrigin().getEndpoint().getServerAddress()); 
            selectedHosts.add(((Result<?>) r).getOrigin());
          }
        } 
        ctx.getCorus().getContext().getSelectedHosts().push(selectedHosts);
        Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
        interpreter.setAutoClusterInfo(info);
        try {
          interpreter.interpret(script, ctx.getVars());
        } catch (AbortException e) {
          throw e;
        } catch (CommandNotFoundException e) {
          throw new InputException("Command not found: " + e.getMessage());
        } catch (InputException e) {
          throw e;
        } catch (Throwable e) {
          throw new AbortException("Error executing script", e);
        } finally {
          ctx.getCorus().getContext().getSelectedHosts().pop();
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
