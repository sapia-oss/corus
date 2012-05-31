package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Command;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.cli.SystemExitException;


/**
 * @author Yanick Duchesne
 */
public abstract class CorusCliCommand implements Command {
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
  
  protected ClusterInfo getClusterInfo(CliContext ctx) {
  	ClusterInfo info = new ClusterInfo(
  	    ctx.getCommandLine().containsOption(CLUSTER_OPT, false)
    );
    return info;
  }
  
  protected void sleep(long millis) throws AbortException {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
      throw new AbortException();
    }
  }
}
