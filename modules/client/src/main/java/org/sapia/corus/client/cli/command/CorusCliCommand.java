package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdElement;
import org.sapia.console.CmdLine;
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
import org.sapia.ubik.util.Func;

/**
 * @author Yanick Duchesne
 */
public abstract class CorusCliCommand implements Command {
  
  /**
   * Describes a command-line option.
   * 
   * @author yduchesne
   *
   */
  public static final class OptionDef implements Comparable<OptionDef> {
    
    private String name;
    private boolean mustHaveValue;
    
    /**
     * @param name the option name.
     * @param mustHaveValue if <code>true</code>, indicates that a value must be provided.
     */
    public OptionDef(String name, boolean mustHaveValue) {
      this.name = name;
      this.mustHaveValue = mustHaveValue;
    }
    
    /**
     * @return the name of this instance's corresponding option.
     */
    public String getName() {
      return name;
    }
    
    /**
     * @return <code>true</code> if options corresponding to this instance 
     * must have a value.
     */
    public boolean mustHaveValue() {
      return mustHaveValue;
    }
    
    @Override
    public String toString() {
      return name;
    }
    
    @Override
    public int hashCode() {
      return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof OptionDef) {
        OptionDef def = (OptionDef) obj;
        return name.equals(def.getName());
      }
      return false;
    }
    
    @Override
    public int compareTo(OptionDef o) {
      return name.compareTo(o.getName());
    }
    
  }
  
  // --------------------------------------------------------------------------

  public static final OptionDef OPT_CLUSTER           = new OptionDef("cluster", false);
  public static final OptionDef OPT_DIST              = new OptionDef("d", true);
  public static final OptionDef OPT_VERSION           = new OptionDef("v", true);
  public static final OptionDef OPT_PROFILE           = new OptionDef("p", true);
  public static final OptionDef OPT_PROCESS_NAME      = new OptionDef("n", true);
  public static final OptionDef OPT_PROCESS_ID        = new OptionDef("i", true);
  public static final OptionDef OPT_PROCESS_INSTANCES = new OptionDef("i", true);
  public static final OptionDef OPT_OS_PID            = new OptionDef("op", true);
  
  public static final String ARG_ALL   = "all";
  public static final String WILD_CARD = "*";

  private static final String NO_VALIDATE = "no-validate";
  
  private volatile boolean initialized;

  
  /**
   * @see org.sapia.console.Command#execute(Context)
   */
  public void execute(Context ctx) throws AbortException, InputException {
    CliContext cliCtx = (CliContext) ctx;    
    if (!initialized) {
      doInit(cliCtx);
      initialized = true;
    }

    
    
    try {
      validate(ctx.getCommandLine());      
      doExecute(cliCtx);
    } catch (InputException ie) {
      CliError err = cliCtx.createAndAddErrorFor(this, ie);
      ctx.getConsole().println(err.getSimpleMessage());
    } catch (AbortException ae) {
      if (ae instanceof SystemExitException) {
        cliCtx.removeAllErrors();
      } else {
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

  /**
   * This method is called only at this instance's first {@link #doExecute(CliContext)} invocation.
   * 
   * @param context the {@link CliContext} corresponding to the current invocation.
   * 
   */
  protected abstract void doInit(CliContext context);
  
  /**
   * @param ctx the current {@link CliContext}.
   * @throws AbortException if the CLI should be terminated.
   * @throws InputException if wrong input as been passed.
   */
  protected abstract void doExecute(CliContext ctx) throws AbortException, InputException;

  protected void displayProgress(ProgressQueue queue, CliContext ctx) {
    ProgressMsg msg;
    List<ProgressMsg> msgs;

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
    if (ctx.getCommandLine().containsOption(OPT_CLUSTER.getName(), false)) {
      Option opt = ctx.getCommandLine().assertOption(OPT_CLUSTER.getName(), false);
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
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      throw new AbortException("Thread interrupted");
    }
  }

  protected static Option getOpt(CliContext ctx, String name, String defaultVal) throws InputException {
    if (ctx.getCommandLine().containsOption(name, true)) {
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
   * @param ctx
   *          the {@link CliContext}.
   * @param name
   *          the name of the option whose value should be returned.
   * @return the option's value, or <code>null</code> if it has no value.
   * @throws InputException
   */
  protected static String getOptValue(CliContext ctx, String name) throws InputException {
    if (ctx.getCommandLine().containsOption(name, true)) {
      return ctx.getCommandLine().assertOption(name, true).getValue();
    } else {
      return null;
    }
  }

  /**
   * Extracts the values of an option that is specifed as a comma-delimited list
   * of elements.
   * 
   * @param ctx
   *          the {@link CliContext}.
   * @param name
   *          the name of the option whose value should be used.
   * @param converter
   *          the converter {@link Func}, used to convert the option value
   *          to a list of strongly-typed elements.
   * @return the {@link List} of values that to which the option's value was
   *         converted.
   * @throws InputException
   *           if no value was specified for the given option.
   */
  protected static <T> List<T> getOptValues(CliContext ctx, String name, Func<T, String> converter) throws InputException {
    if (ctx.getCommandLine().containsOption(name, true)) {
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
   * @param ctx
   *          the {@link CliContext}.
   * @return the first {@link Arg} in the command-line, or <code>null</code> if
   *         no such instance exists.
   */
  protected Arg getFirstArg(CliContext ctx) {
    while (ctx.getCommandLine().hasNext()) {
      CmdElement e = ctx.getCommandLine().next();
      if (e instanceof Arg) {
        return (Arg) e;
      }
    }
    return null;
  }
  
  protected void validate(CmdLine cmdLine) throws InputException {
    if (!cmdLine.containsOption(NO_VALIDATE, false)) {
      List<OptionDef> availableOptions = getAvailableOptions();
      Map<String, OptionDef> byName = new HashMap<String, CorusCliCommand.OptionDef>();
      for (OptionDef a : availableOptions) {
        byName.put(a.name, a);
      }
      for (int i = 0; i < cmdLine.size(); i++) {
        CmdElement e = cmdLine.get(i);
        if (e instanceof Option) {
          Option o = (Option) e;
          OptionDef d = byName.get(o.getName());
          if (d == null) {
            if (availableOptions.isEmpty()) {
              throw new InputException("Command does not support options");
            } else {
              Set<OptionDef> sorted = new TreeSet<CorusCliCommand.OptionDef>(availableOptions); 
              throw new InputException(String.format("Unsupported option: %s. Supported options are: %s", o.getName(), sorted));
            }
          }
          if (d.mustHaveValue && (o.getValue() == null || o.getValue().trim().length() == 0)) {
            throw new InputException(String.format("Option %s must have a value", o.getName()));
          }
        }
      }
    }
  }
  
  protected abstract List<OptionDef> getAvailableOptions();
}
