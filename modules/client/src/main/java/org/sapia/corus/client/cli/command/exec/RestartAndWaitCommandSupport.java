package org.sapia.corus.client.cli.command.exec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

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
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_CLUSTER, OPT_WAIT, OPT_HARD_KILL);
  }

  /**
   * @param ctx
   *          the {@link CliContext}.
   * @param cluster
   *          a {@link ClusterInfo}.
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param waitSeconds
   *          the number of seconds to wait for.
   * @throws AbortException
   * @throws InputException
   */
  protected final void doRestartAndWait(CliContext ctx, ClusterInfo cluster, ProcessCriteria criteria, KillPreferences prefs, int waitSeconds) throws AbortException,
      InputException {

    Results<List<Process>> instances = ctx.getCorus().getProcessorFacade().getProcesses(criteria, cluster);

    Map<ServerAddress, Map<DistributionInfo, AtomicInteger>> processesByHosts = new HashMap<ServerAddress, Map<DistributionInfo, AtomicInteger>>();

    while (instances.hasNext()) {
      Result<List<Process>> processList = instances.next();

      Map<DistributionInfo, AtomicInteger> processes = processesByHosts.get(processList.getOrigin());
      if (processes == null) {
        processes = new HashMap<DistributionInfo, AtomicInteger>();
        processesByHosts.put(processList.getOrigin().getEndpoint().getServerAddress(), processes);
      }
      for (Process p : processList.getData()) {
        AtomicInteger processCount = processes.get(p.getDistributionInfo());
        if (processCount == null) {
          processCount = new AtomicInteger();
          processes.put(p.getDistributionInfo(), processCount);
        }
        processCount.incrementAndGet();
      }
    }

    ctx.getCorus().getProcessorFacade().restart(criteria, prefs, cluster);

    waitForProcessShutdown(ctx, criteria, waitSeconds, cluster);

    for (ServerAddress node : processesByHosts.keySet()) {
      Map<DistributionInfo, AtomicInteger> processes = processesByHosts.get(node);
      for (DistributionInfo proc : processes.keySet()) {
        criteria = ProcessCriteria.builder().distribution(proc.getName()).profile(proc.getProfile()).name(proc.getProcessName())
            .version(proc.getVersion()).build();
        ClusterInfo subset = new ClusterInfo(true);
        subset.addTarget(node);
        waitForProcessStartup(ctx, criteria, processes.get(proc).get(), waitSeconds, subset);

      }
    }
  }

}
