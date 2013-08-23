package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdElement;
import org.sapia.console.Command;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.cli.SystemExitException;
import org.sapia.ubik.util.Function;


/**
 * @author Yanick Duchesne
 */
public abstract class CorusCliCommand implements Command {
  
  public static final int CONSOLE_WIDTH   = 80;
  
  public static final String CLUSTER_OPT  = "cluster";
  public static final String DIST_OPT     = "d";
  public static final String VERSION_OPT  = "v";
  public static final String PROFILE_OPT  = "p";
  public static final String VM_NAME_OPT  = "n";
  public static final String VM_ID_OPT    = "i";
  public static final String VM_INSTANCES = "i";
  public static final String ARG_ALL      = "all";
  public static final String WILD_CARD    = "*";
  
  public static final String OS_PID_OPT   = "op";
  
  /**
   * @see org.sapia.console.Command#execute(Context)
   */
  public void execute(Context ctx) throws AbortException, InputException {
    CliContext cliCtx = (CliContext)ctx;
    
    try {
      doExecute(cliCtx);
      
    } catch (InputException ie) {
      CliError err = cliCtx.createAndAddErrorFor(this, ie);
      ctx.getConsole().println(err.getSimpleMessage());
    } catch (AbortException ae) {
      if (!(ae instanceof SystemExitException)) {
        CliError err = cliCtx.createAndAddErrorFor(this, ae);
        ctx.getConsole().println(err.getSimpleMessage());
      }
      throw ae;
      
    } catch (RuntimeException re) {
      CliError err = cliCtx.createAndAddErrorFor(this, re);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }
  
  public String getName() {
    return getClass().getSimpleName().toLowerCase();
  }

  protected abstract void doExecute(CliContext ctx)
                             throws AbortException, InputException;

  protected void displayProgress(ProgressQueue queue, CliContext ctx) {
    ProgressMsg msg;
    List<ProgressMsg>        msgs;

    while (queue.hasNext()) {
      msgs = queue.next();

      for (int i = 0; i < msgs.size(); i++) {
        msg = (ProgressMsg) msgs.get(i);

        if (msg.isThrowable()) {
          CliError err = ctx.createAndAddErrorFor(this, (Throwable) msg.getMessage());
          ctx.getConsole().println(err.getSimpleMessage());
          
        } else if (msg.getStatus() >= ProgressMsg.INFO) {
          ctx.getConsole().println(msg.getMessage().toString());
        }
      }
    }
  }
  
  protected ClusterInfo getClusterInfo(CliContext ctx) throws InputException {
    if (ctx.getCommandLine().containsOption(CLUSTER_OPT, false)) {
      Option opt = ctx.getCommandLine().assertOption(CLUSTER_OPT, false);
      if (opt.getValue() != null) {
        ClusterInfo info = new ClusterInfo(true);
        info.addTargets(CliUtils.parseServerAddresses(opt.getValue()));
        return info;
      } else {
        return new ClusterInfo(true);
      }
    } else {
      return new ClusterInfo(false);
    }
  }
  
  protected static void sleep(long millis) throws AbortException {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      throw new AbortException();
    }
  }
  
  protected static Option getOpt(CliContext ctx, String name, String defaultVal) throws InputException {
    if (ctx.getCommandLine().containsOption(name,true)) {
      return ctx.getCommandLine().assertOption(name, true);
    } else {
      return new Option(name, defaultVal);
    }
  }
  
  protected static Option getOpt(CliContext ctx, String name) throws InputException {
    if (ctx.getCommandLine().containsOption(name, false)) {
      return ctx.getCommandLine().assertOption(name, false);
    } else {
      return null;
    }
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @param name the name of the option whose value should be returned.
   * @return the option's value, or <code>null</code> if it has no value.
   * @throws InputException
   */
  protected static String getOptValue(CliContext ctx, String name) throws InputException {
    if (ctx.getCommandLine().containsOption(name,true)) {
      return ctx.getCommandLine().assertOption(name, true).getValue();
    } else {
      return null;
    }
  }
  
  /**
   * Extracts the values of an option that is specifed as a comma-delimited list of elements.
   * 
   * @param ctx the {@link CliContext}.
   * @param name the name of the option whose value should be used.
   * @param converter the converter {@link Function}, used to convert the option value to a list
   * of strongly-typed elements.
   * @return the {@link List} of values that to which the option's value was converted.
   * @throws InputException if no value was specified for the given option.
   */
  protected static <T> List<T> getOptValues(CliContext ctx, String name, Function<T, String> converter) throws InputException {
    if (ctx.getCommandLine().containsOption(name,true)) {
      String[] valueList = ctx.getCommandLine().assertOption(name, true).getValue().split(",");
      List<T> toReturn = new ArrayList<T>(valueList.length);
      for (String v : valueList) {
        toReturn.add(converter.call(v));
      }
      return toReturn;
    } else {
      return new ArrayList<T>();
    }
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @return the first {@link Arg} in the command-line, or <code>null</code> if no such instance exists.
   */
  protected Arg getFirstArg(CliContext ctx) {
    while (ctx.getCommandLine().hasNext()) {
      CmdElement e = ctx.getCommandLine().next();
      if (e instanceof Arg) {
        return (Arg)e;
      }
    }
    return null;
  } 
}
