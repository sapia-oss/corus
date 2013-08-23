package org.sapia.corus.client.cli.command.exec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements restart-an-wait logic. 
 * 
 * 
 * @author yduchesne
 *
 */
abstract class RestartAndWaitCommandSupport extends AbstractExecCommand {
  
  protected RestartAndWaitCommandSupport() {
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @param cluster a {@link ClusterInfo}.
   * @param criteria a {@link ProcessCriteria}.
   * @param waitSeconds the number of seconds to wait for.
   * @throws AbortException
   * @throws InputException
   */
  protected final void doRestartAndWait(CliContext ctx, ClusterInfo cluster, ProcessCriteria criteria, int waitSeconds) throws AbortException,
      InputException {
    
      List<Process> instances = getProcessInstances(ctx.getCorus().getProcessorFacade(), criteria, cluster);

      Map<DistributionInfo, AtomicInteger> processCounts = new HashMap<DistributionInfo, AtomicInteger>();
      
      for (Process i : instances) {
        AtomicInteger count = processCounts.get(i.getDistributionInfo());
        if (count == null) {
          count = new AtomicInteger(1);
          processCounts.put(i.getDistributionInfo(), count);
        } else {
          count.incrementAndGet();
        }
      }
      
      ctx.getCorus().getProcessorFacade().restart(criteria, cluster);

      waitForProcessShutdown(
          ctx, 
          criteria, 
          waitSeconds, 
         cluster);
      
      for (Map.Entry<DistributionInfo, AtomicInteger> proc : processCounts.entrySet()) {
        criteria = ProcessCriteria.builder()
            .distribution(proc.getKey().getName())
            .profile(proc.getKey().getProfile())
            .name(proc.getKey().getProcessName())
            .version(proc.getKey().getVersion())
            .build();
        
        waitForProcessStartup(
            ctx, 
            criteria, 
            proc.getValue().get(), 
            waitSeconds, 
            cluster);        
      }
      
      
  }

}
