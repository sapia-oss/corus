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
    List<Process> currentProcesses = getProcessInstances(ctx.getCorus().getProcessorFacade(), criteria, cluster);
    
    Set<String> existing = new HashSet<String>();
    for (Process p : currentProcesses) {
      existing.add(p.getOsPid() + ":" + p.getProcessID());
    }
    
    Delay delay = new Delay(seconds, TimeUnit.SECONDS);    
    int oldProcessCount = 0;
    do {
      currentProcesses = getProcessInstances(ctx.getCorus().getProcessorFacade(), criteria, cluster);
      oldProcessCount = 0;
      for (Process p : currentProcesses) {
        if(existing.contains(p.getOsPid() + ":" + p.getProcessID())) {
          oldProcessCount++;
        }
      }
      sleep(WAIT_INTERVAL_MILLIS);
    } while (oldProcessCount > 0 && delay.isNotOver());
  }
  
  /**
   * @param ctx the {@link CliContext}.
   * @param criteria a {@link ProcessCriteria}.
   * @param instancesPerHost the expected number of started instances per host.
   * @param seconds the max number of seconds to wait for.
   * @param cluster a {@link ClusterInfo}.
   */  
  protected final void waitForProcessStartup(CliContext ctx, ProcessCriteria criteria, int instancesPerHost, int seconds, ClusterInfo cluster) {
    Delay delay = new Delay(seconds, TimeUnit.SECONDS);
    
    
    // determining which hosts may have the running processes: they must have the
    // distribution, and the tags corresponding to these processes
    Results<List<Distribution>> distResults = ctx.getCorus().getDeployerFacade().getDistributions(
        criteria.getDistributionCriteria(), 
        cluster);
    Set<Distribution> dists = new HashSet<Distribution>();
    
    Map<ServerAddress, Set<String>> hostsWithDist = new HashMap<ServerAddress, Set<String>>();
    while (distResults.hasNext()) {
      Result<List<Distribution>> distResult = distResults.next();
      if (!distResult.getData().isEmpty()) {
          dists.addAll(distResult.getData());
      }
      hostsWithDist.put(distResult.getOrigin(), new HashSet<String>());
    }
    
    // distribution set should not be empty, but considering anyway
    if (!dists.isEmpty()) {
      
      // obtaining the tags for each targeted host in the cluster
      Results<Set<String>> tagResults = ctx.getCorus().getConfigFacade().getTags(cluster);
      
      for (Result<Set<String>> t : tagResults) {
        Set<String> hostTags = hostsWithDist.get(t.getOrigin());
        if (hostTags != null) {
          hostTags.addAll(t.getData());
        }
      }
     
      // now checking which hosts are supposed to run the processes, based on the tags
      Set<ServerAddress> targets = new HashSet<ServerAddress>();
      for (Distribution dist : dists) {
        
        List<ProcessConfig> procConfigs = dist.getProcesses(criteria.getName());
        for (ProcessConfig procConfig : procConfigs) {
          for (ServerAddress host : hostsWithDist.keySet()) {
            Set<String> hostTags = hostsWithDist.get(host);
            Set<String> procTags = new HashSet<String>();
            procTags.addAll(dist.getTagSet());
            procTags.addAll(procConfig.getTagSet());
            if (procTags.isEmpty() || hostTags.containsAll(procTags)) {
              targets.add(host);
            } 
          }
        }
      }
      
      // we've got the target hosts: checking on them
      if (instancesPerHost > 0 && !targets.isEmpty()) {
        int totalExpectedInstances = instancesPerHost * targets.size();
        int currentCount = 0;
        ClusterInfo targetInfo = new ClusterInfo(true);
        targetInfo.addTargets(targets);
        while (delay.isNotOver()) {
          currentCount = 0;
          Results<List<Process>> processes = ctx.getCorus().getProcessorFacade().getProcesses(criteria, targetInfo);
          while (processes.hasNext()) {
            Result<List<Process>> proc = processes.next();
            currentCount += proc.getData().size();
          }
          if (currentCount < totalExpectedInstances) {
            sleep(WAIT_INTERVAL_MILLIS);
          } else {
            break;
          }
        }
        
        if (currentCount < totalExpectedInstances) {
          throw new IllegalStateException("Expected number of processes not started. Got: " + currentCount + ", expected: " + totalExpectedInstances);
        } else {
          ctx.getConsole().println(String.format("Completed startup of %s process(es) on: %s", totalExpectedInstances, targets));          
        }
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
      Result<List<Process>> processList = processes.next();
      if (processList == null) {
        break;
      }
      instances += processList.getData().size();        
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
      Result<List<Process>> processList = processes.next();
      if (processList == null) {
        break;
      }
      instances.addAll(processList.getData());        
    } 
    return instances;
  }  
  
}
