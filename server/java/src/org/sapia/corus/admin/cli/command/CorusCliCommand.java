package org.sapia.corus.admin.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Command;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class CorusCliCommand implements Command {
  public static final String CLUSTER_OPT  = "cluster";
  public static final String DIST_OPT     = "d";
  public static final String VERSION_OPT  = "v";
  public static final String PROFILE_OPT  = "p";
  public static final String VM_NAME_OPT  = "n";
  public static final String VM_ID_OPT    = "i";
  public static final String VM_INSTANCES = "i";
  public static final String OS_PID_OPT   = "op";
  
  /**
   * @see org.sapia.console.Command#execute(Context)
   */
  public void execute(Context ctx) throws AbortException, InputException {
    doExecute((CliContext) ctx);
  }

  protected abstract void doExecute(CliContext ctx)
                             throws AbortException, InputException;

  protected static void displayProgress(ProgressQueue queue, Console cons) {
    ProgressMsg msg;
    List        msgs;

    while (queue.hasNext()) {
      msgs = queue.next();

      for (int i = 0; i < msgs.size(); i++) {
        msg = (ProgressMsg) msgs.get(i);

        if (msg.isThrowable()) {
          ((Throwable) msg.getMessage()).printStackTrace(cons.out());
        } else if (msg.getStatus() >= ProgressMsg.INFO) {
          cons.println(msg.getMessage().toString());
        }
      }
    }
  }
  
  protected ClusterInfo getClusterInfo(CliContext ctx) /*throws InputException*/{
    /*if(ctx.getCommandLine().containsOption(CLUSTER_OPT, true)){
      String clusterOpt = ctx.getCommandLine().assertOption(CLUSTER_OPT, true).getValue();
      String[] tags = clusterOpt.split(",");
      ClusterInfo info = new ClusterInfo(true);
      for(int i = 0; i < tags.length; i++){
        info.addTag(tags[i].trim());
      }
      return info;
    }
    else{*/
    	ClusterInfo info = new ClusterInfo(
       ctx.getCommandLine().containsOption(CLUSTER_OPT, false)
      );
      return info;
    //}
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
