package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.AutoClusterFlag;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CorusConsoleOutput;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.cli.StrLookupState;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.CompositePattern;
import org.sapia.corus.client.common.StrLookups;
import org.sapia.corus.client.facade.FacadeInvocationContext;
import org.sapia.ubik.util.Func;

/**
 * Displays the count of items in the result of a command.
 * 
 * @author yduchesne
 *
 */
public class Foreach extends NoOptionCommand {
  
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
    Func<Reader, Void> script     = null;
    if (arg.getName().equals(APPLY)) {
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<script_file_name> expected after 'apply'");
      }
      String scriptName = cmd.assertNextArg().getName();
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<pattern> expected after 'apply'");
      }
      patternArg = cmd.assertNextArg();
      
      final File scriptFile  = ctx.getFileSystem().getFile(scriptName);
      if (!scriptFile.exists()) {
        throw new InputException("Script not found: " + scriptName);
      }
      
      script = new Func<Reader, Void>() {
        @Override
        public Reader call(Void in) {
          try {
            return new FileReader(scriptFile);
          } catch (IOException e) {
            throw new IllegalStateException("Could not load script: " + scriptFile.getAbsolutePath(), e);
          }
        }
      }; 
      
    } else if (arg.getName().equals(COMMAND)) {
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("\"<command_line>\" expected after 'apply'");
      }
      final String command = cmd.assertNextArg().getName();
      if (!cmd.hasNext() || !cmd.isNextArg()) {
        throw new InputException("<pattern> expected after 'apply'");
      }
      patternArg = cmd.assertNextArg();

      script = new Func<Reader, Void>() {
        @Override
        public Reader call(Void arg0) {
          return new StringReader(command.replace("'", "\""));
        }
      };
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
          script.call(null).close();
        }
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void doExecute(CliContext ctx, Func<Reader, Void> script, Arg patternArg, CmdLine remaining) throws IOException, AbortException, InputException {
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
      if (ctx.getCorus().getContext().getSelectedHosts().peek().isSet()) {
        intp.setAutoClusterInfo(
            AutoClusterFlag.forExplicit(
                ClusterInfo.clustered()
                  .addTargetHosts(ctx.getCorus().getContext().getSelectedHosts().peek().get())
            )
        );
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
    
    if (script != null && FacadeInvocationContext.get() != null) {
      List<Object> flattenedResult = new ArrayList<>();
      Object response = FacadeInvocationContext.get(); 
      if (response instanceof Results) {
        flattenedResult.addAll(((Results) response).flatten());
      } else if (response instanceof Iterable) {
        for (Object r : (Iterable) response) {
          if (r instanceof Result) {
            Result<?> res = (Result<?>) r;
            if (res.getData() instanceof Iterable) {
              for (Object data : (Iterable) res.getData()) {
                flattenedResult.add(data);
              }
            } else if (res.getData() instanceof Object[]) {
              for (Object data : (Object[]) res.getData()) {
                flattenedResult.add(data);
              }
            } else {
              flattenedResult.add(res.getData());
            }
          } else {
            flattenedResult.add(r);
          }
        } 
      }
      
      if (!flattenedResult.isEmpty()) {
        try {
          for (Object r : flattenedResult) {
            Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
            if (ctx.getCorus().getContext().getSelectedHosts().peek().isSet()) {
              interpreter.setAutoClusterInfo(
                  AutoClusterFlag.forExplicit(
                      ClusterInfo.clustered()
                        .addTargetHosts(ctx.getCorus().getContext().getSelectedHosts().peek().get()) 
                  )
              );
            }
            
            StrLookupState state;
            if (r instanceof Mappable) {
              Map<String, Object> values = ((Mappable) r).asMap();
              state = new StrLookupState(StrLookups.merge(StrLookup.mapLookup(values), ctx.getVars().get()));  
            } else {
              state = ctx.getVars();
            }
            
            interpreter.interpret(script.call(null), state);
          }
        } catch (AbortException e) {
          throw e;
        } catch (CommandNotFoundException e) {
          throw new InputException("Command not found: " + e.getMessage());
        } catch (InputException e) {
          throw e;
        } catch (Throwable e) {
          throw new AbortException("Error executing script", e);
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
