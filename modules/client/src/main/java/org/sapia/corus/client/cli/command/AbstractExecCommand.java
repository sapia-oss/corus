package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;

/**
 * Base class implementing logic for blocking until process startup completion. 
 * 
 * @author yduchesne
 *
 */
public abstract class AbstractExecCommand extends CorusCliCommand {
  
  protected static final long WAIT_INTERVAL_MILLIS = 5000;
  protected static final String OPT_WAIT           = "w";

  protected AbstractExecCommand() {
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @param criteria a {@link ProcessCriteria}.
   * @param seconds the max number of seconds to wait for.
   * @param cluster a {@link ClusterInfo}.
   */
  protected final void waitForProcessShutdown(CliContext ctx, ProcessCriteria criteria, int seconds, ClusterInfo cluster) {
    Delay delay = new Delay(seconds, TimeUnit.SECONDS);    
    int instances = 0;
    do {
      instances = getProcessInstanceCount(ctx.getCorus().getProcessorFacade(), criteria, cluster);
    } while (instances > 0 && delay.isNotOver());
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @param criteria a {@link ProcessCriteria}.
   * @param startedInstances the expected number started instances.
   * @param seconds the max number of seconds to wait for.
   * @param cluster a {@link ClusterInfo}.
   */  
  protected final void waitForProcessStartup(CliContext ctx, ProcessCriteria criteria, int startedInstances, int seconds, ClusterInfo cluster) {
    Delay delay = new Delay(seconds, TimeUnit.SECONDS);
    
    
    // determining which hosts may have the running processes: they must have the
    // distribution, and the tags corresponding to these processes
    Results<List<Distribution>> distResults = ctx.getCorus().getDeployerFacade().getDistributions(
        criteria.getDistributionCriteria(), 
        cluster);
    Distribution dist = null;
    
    Map<ServerAddress, Set<String>> hostsWithDist = new HashMap<ServerAddress, Set<String>>();
    while (distResults.hasNext()) {
      Result<List<Distribution>> distResult = distResults.next();
      if (!distResult.getData().isEmpty()) {
        if (dist == null) {
          dist = distResult.getData().get(0);
        }
        hostsWithDist.put(distResult.getOrigin(), new HashSet<String>());
      }
    }
    
    // distribution should not be null, but considering anyway
    if (dist != null) {
      
      // obtaining the tags for each targeted host in the cluster
      Results<Set<String>> tagResults = ctx.getCorus().getConfigFacade().getTags(cluster);
      
      for (Result<Set<String>> t : tagResults) {
        Set<String> hostTags = hostsWithDist.get(t.getOrigin());
        if (hostTags != null) {
          hostTags.addAll(t.getData());
        }
      }
     
      // now checking which hosts are supposed to run the processes, based on the tags
      List<ProcessConfig> procConfigs = dist.getProcesses(criteria.getName());
      Set<ServerAddress> targets = new HashSet<ServerAddress>();
      for (ProcessConfig procConfig : procConfigs) {
        for (ServerAddress host : hostsWithDist.keySet()) {
          Set<String> hostTags = hostsWithDist.get(host);
          if (procConfig.getTagSet().isEmpty() || hostTags.containsAll(procConfig.getTagSet())) {
            targets.add(host);
          } 
        }
      }
      
      // we've got the target hosts: checking on them
      int expectedNumberOfProcesses = targets.size() * startedInstances;
      if (expectedNumberOfProcesses > 0) {
        int currentCount = 0;
        ClusterInfo targetInfo = new ClusterInfo(true);
        targetInfo.addTargets(targets);
        ctx.getConsole().println(String.format("Waiting for startup: expecting a total of %s process(es) on %s Corus node(s)", expectedNumberOfProcesses, targets.size()));
        while (delay.isNotOver()) {
          currentCount = 0;
          Results<List<Process>> processes = ctx.getCorus().getProcessorFacade().getProcesses(criteria, targetInfo);
          while (processes.hasNext()) {
            Result<List<Process>> proc = processes.next();
            currentCount += proc.getData().size();
          }
          if (currentCount < expectedNumberOfProcesses) {
            sleep(WAIT_INTERVAL_MILLIS);
          } else {
            break;
          }
        }
        
        if (currentCount < expectedNumberOfProcesses) {
          throw new IllegalStateException("Expected number of processes not started. Got: " + currentCount + ", expected: " + expectedNumberOfProcesses);
        } else {
          ctx.getConsole().println(String.format("All processes started (got %s)", currentCount));
        }
      } else {
        ctx.getConsole().println("No process will be started (are process tags matching Corus tags?)");
      }
    } 
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @return the {@link Option} corresponding to the <code>-w</code> command-line option.
   */
  protected static Option getWaitOption(CliContext ctx) throws InputException {
    return getOpt(ctx, OPT_WAIT);
  } 

  /**
   * @param processor a {@link ProcessorFacade}.
   * @param criteria a {@link ProcessCriteria}.
   * @param cluster a {@link ClusterInfo}.
   * @return the number of instances corresponding to the given criteria.
   */
  protected static int getProcessInstanceCount(ProcessorFacade processor, ProcessCriteria criteria, ClusterInfo cluster) {
    Results<List<Process>> processes = processor.getProcesses(criteria, cluster);
    int instances = 0;
    while(processes.hasNext()) {
      instances += processes.next().getData().size();
    } 
    return instances;
  }
  
  /**
   * @param processor a {@link ProcessorFacade}.
   * @param criteria a {@link ProcessCriteria}.
   * @param cluster a {@link ClusterInfo}.
   * @return the {@link List} of {@link Process} instances.
   */
  protected static List<Process> getProcessInstances(ProcessorFacade processor, ProcessCriteria criteria, ClusterInfo cluster) {
    Results<List<Process>> processes = processor.getProcesses(criteria, cluster);
    List<Process> instances = new ArrayList<Process>();
    while(processes.hasNext()) {
      instances.addAll(processes.next().getData());
    } 
    return instances;
  }  
}
