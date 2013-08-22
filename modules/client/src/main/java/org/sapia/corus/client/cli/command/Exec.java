package org.sapia.corus.client.cli.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.ubik.net.ServerAddress;

/**
 * Executes processes.
 * 
 * @author Yanick Duchesne
 */
public class Exec extends CorusCliCommand {

  private static final String OPT_EXEC_CONFIG = "e";
  private static final String OPT_SCRIPT      = "s";
  private static final String OPT_WAIT        = "w";
  
  private static final int  DEFAULT_WAIT_TIME_SECONDS = 120;
  private static final long WAIT_INTERVAL_MILLIS      = 5000;
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    if(ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG, true)) {
      doExecuteConfig(ctx);
    } else if(ctx.getCommandLine().containsOption(OPT_SCRIPT, true)) {
      doExecuteScript(ctx);
    } else {
      doExecuteProcesses(ctx);
    }
  }

  private void doExecuteConfig(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String configName = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG, true).getValue();
    displayProgress(ctx.getCorus().getProcessorFacade().execConfig(configName, cluster), ctx);
    
    // determining which hosts may have the running processes: they must have the
    // distribution, and the tags corresponding to these processes
    Results<List<ExecConfig>> execResults = ctx.getCorus().getProcessorFacade().getExecConfigs(cluster);
    
    ExecConfig conf = null;
    while (execResults.hasNext()) {
      Result<List<ExecConfig>> execResult = execResults.next();
      for (ExecConfig e : execResult.getData()) {
        if (e.getName().equals(configName)) {
          conf = e;
        }
      }
    }

    Option waitOpt = getOpt(ctx, OPT_WAIT);
    if (waitOpt != null) {
      for (ProcessDef pd : conf.getProcesses()) {
        ProcessCriteria criteria = ProcessCriteria.builder()
          .distribution(pd.getDist())
          .name(pd.getName())
          .profile(conf.getProfile())
          .version(pd.getVersion())
          .build();
        
        waitForProcessStartup(ctx, criteria, 1, waitOpt.getValue() == null ? DEFAULT_WAIT_TIME_SECONDS : waitOpt.asInt(), cluster);
      }
      ctx.getConsole().println("Process startup completed");
    }
  } 
  
  private void doExecuteScript(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String alias = ctx.getCommandLine().assertOption(OPT_SCRIPT, true).getValue();
    try {
      displayProgress(ctx.getCorus().getScriptManagementFacade().execScript(alias, cluster), ctx);
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be executed: " + alias, e);
    } catch (ScriptNotFoundException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be found for alias: " + alias, e);
    }
  }  
  
  private void doExecuteProcesses(CliContext ctx) throws AbortException, InputException {
    String  dist      = null;
    String  version   = null;
    String  profile   = null;
    String  vmName    = null;
    int     instances = 1;
    CmdLine cmd       = ctx.getCommandLine();

    dist = cmd.assertOption(DIST_OPT, true).getValue();

    version = cmd.assertOption(VERSION_OPT, true).getValue();

    profile = cmd.assertOption(PROFILE_OPT, true).getValue();

    if (cmd.containsOption(VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(VM_INSTANCES, true)) {
      instances = cmd.assertOption(VM_INSTANCES, true).asInt();
    }

    ClusterInfo cluster = getClusterInfo(ctx);
    ProcessCriteria criteria = ProcessCriteria.builder()
      .name(vmName)
      .distribution(dist)
      .version(version)
      .profile(profile)
      .build();
    try {
      displayProgress(
              ctx.getCorus().getProcessorFacade().exec(criteria, instances, cluster),
              ctx
      );
      
      Option waitOpt = getOpt(ctx, OPT_WAIT);
      if (waitOpt != null) {
        waitForProcessStartup(ctx, criteria, instances, waitOpt.getValue() == null ? DEFAULT_WAIT_TIME_SECONDS : waitOpt.asInt(), cluster);
        ctx.getConsole().println("Process startup completed");        
      }
      
    } catch(TooManyProcessInstanceException e){
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    } 
    
  }
  
  private void waitForProcessStartup(CliContext ctx, ProcessCriteria criteria, int startedInstances, int seconds, ClusterInfo cluster) {
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
      Set<String> procNames = new HashSet<String>();
      Set<ServerAddress> targets = new HashSet<ServerAddress>();
      for (ProcessConfig procConfig : procConfigs) {
        procNames.add(procConfig.getName());
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
        ctx.getConsole().println(String.format("Waiting for startup of %s process(es). Expecting a total of %s process(es) on %s Corus node(s)", procNames, expectedNumberOfProcesses, targets.size()));
        while (!delay.isOver()) {
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
          ctx.getConsole().println(String.format("%s processes all started (got %s)", procNames, currentCount));
        }
      } else {
        ctx.getConsole().println("No process will be started (are process tags matching Corus tags?)");
      }
    } 
  }
  
}
