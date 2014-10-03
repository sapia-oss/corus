package org.sapia.corus.client.cli.command;

import java.util.Iterator;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CorusConsoleOutput;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.facade.FacadeInvocationContext;

/**
 * Displays the count of items in the result of a command.
 * 
 * @author yduchesne
 *
 */
public class Count extends NoOptionCommand {
  
  private static final String OPT_ASSERT = "a";
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @SuppressWarnings("unused")
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    CmdLine cmd = ctx.getCommandLine();

    Option assertOpt = null;
    if (cmd.isNextOption()) {
      Option opt = (Option) ctx.getCommandLine().get(0);
      if (opt.getName().equals(OPT_ASSERT)) {
        assertOpt = (Option) ctx.getCommandLine().chop();
        if (assertOpt.getValue() == null) {
          throw new InputException("Expected value for -a option");
        }
      }
    }
    
    String toExecute = ctx.getCommandLine().toString();
    
    try {
      turnOffOutput(ctx);
      Interpreter intp = new Interpreter(ctx.getConsole(), ctx.getCorus());
      intp.eval(toExecute, ctx.getVars());
    } catch (CommandNotFoundException e) {
      throw new InputException("Command not found: " + e.getMessage());
    } catch (InputException e) {
      throw e;
    } catch (Throwable e) {
      throw new AbortException("Could not execute command: " + toExecute, e);
    } finally {
      turnOnOutput(ctx);
    }
    Object returnValue  = FacadeInvocationContext.get();
    int count = 0;
    if(returnValue != null) {
      if (returnValue instanceof Iterable<?>) {
        Iterable<?> results = (Iterable<?>) returnValue;
        Iterator<?> itr = results.iterator();
        while (itr.hasNext()) {
          Object next = itr.next();
          if (next instanceof Result) {
            Result<?> r = (Result<?>) next;
            if (r.getType() == Result.Type.ELEMENT) {
              count++;
            } else {
              if (r.getData() instanceof Iterable) {
                Iterable<?> elements = (Iterable<?>) r.getData();
                for (Object e : elements) {
                  count++;
                }
              } else if (r.getData() instanceof Map) {
                for (Object k : ((Map<?, ?>) r.getData()).keySet()) {
                  count++;
                }
              } else if (r.getData() instanceof Object[]) {
                for (Object o : (Object[]) r.getData()) {
                  count++;
                }
              }
            }
          } else {
            count++;
          }
        }
      }
    } else if (returnValue instanceof Iterable) {
      Iterable<?> itr = (Iterable<?>) returnValue;
      for (Object i : itr) {
        count++;
      }
    }

    FacadeInvocationContext.set(count);
    if (assertOpt != null) {
      int expected = assertOpt.asInt();
      if (count != expected) {
        throw new AbortException(String.format("Expected %s elements in result. Got %s", expected, count));
      }
    }
    ctx.getConsole().out().println(Integer.toString(count));
    
  }
  
  // ---------------------------------------------------------------------------
  // private methods
  
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
