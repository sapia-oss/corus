package org.sapia.corus.client.cli.command.exec;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.Restart;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic for restarting all processes (<code>restart all</code>).
 * 
 * @author yduchesne
 *
 */
public class RestartAllCommand extends RestartAndWaitCommandSupport {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    ClusterInfo     cluster   = getClusterInfo(ctx);
    ProcessCriteria criteria  = ProcessCriteria.builder().all();
    Option          wait      = getWaitOption(ctx);
    
    if (wait != null) {
      ctx.getConsole().println("Waiting for process restart, please stand by...");
      doRestartAndWait(
          ctx, 
          cluster, 
          criteria, 
          wait.getValue() == null ? Restart.DEFAULT_RESTART_WAIT_TIME_SECONDS : wait.asInt());        
      
    } else {
      ctx.getConsole().println("Triggering to process restart...");      
      ctx.getCorus().getProcessorFacade().restart(criteria, cluster);      
    }
  }
  
}
