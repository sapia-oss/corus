package org.sapia.corus.processor.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.BackgroundTaskListener;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.ubik.util.Collects;

/**
 * Implements the bulk of the behavior pertaining to the startup of execution
 * configs.
 * 
 * @author yduchesne
 * 
 */
public abstract class AbstractExecConfigStartTask extends Task<Void, Void> {

  private boolean stopExistingProcesses;

  public AbstractExecConfigStartTask(boolean stopExistingProcesses) {
    this.stopExistingProcesses = stopExistingProcesses;
  }

  @Override
  public Void execute(final TaskExecutionContext ctx, Void param) throws Throwable {
    ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
    Deployer          deployer  = ctx.getServerContext().getServices().getDeployer();

    List<ExecConfig> configsToStart = getExecConfigsToStart(ctx);
    
    configsToStart = Collects.filterAsList(configsToStart, new org.sapia.ubik.util.Condition<ExecConfig>() {
      @Override
      public boolean apply(ExecConfig conf) {
        if (!conf.isEnabled()) {
          ctx.warn("Exec configuration is disabled, its processes will not be started: " + conf.getName());
          return false;
        }
        return true;
      }
    });

    Set<Process> toStop = new HashSet<Process>();
    Set<ProcessDef> toStart = new HashSet<ProcessDef>();

    // finding processes for versions other than the one configured
    // as part of the exec configuration

    for (ExecConfig ec : configsToStart) {
      for (ProcessDef pd : ec.getProcesses()) {
        ArgMatcher distName    = ArgMatchers.exact(pd.getDist());
        ArgMatcher version     = ArgMatchers.exact(pd.getVersion());
        ArgMatcher processName = ArgMatchers.exact(pd.getName());
        if (pd.getProfile() == null) {
          pd.setProfile(ec.getProfile());
        }
        Distribution dist = null;
        
        try {
          dist = deployer.getDistribution(DistributionCriteria.builder().name(distName).version(version).build());
          if (!canExecuteFor(ctx, dist)) {
            continue;
          }
        } catch (DistributionNotFoundException e) {
          ctx.warn("Could not find distribution for exec config: " + ec.getName() + ". Process will not be executed: " + pd);
          continue;
        }
        
        ProcessCriteria criteria = ProcessCriteria.builder()
            .distribution(distName)
            .version(version)
            .name(processName)
            .profile(pd.getProfile())
            .lifecycles(LifeCycleStatus.ACTIVE, LifeCycleStatus.RESTARTING)
            .build();
        
        List<Process> activeProcesses = processes.getProcesses(criteria);
        if (activeProcesses.size() == 0) {
          ctx.debug("Process will be started: " + pd);
          toStart.add(pd);
        } else {
          for (Process p : activeProcesses) {
            if (!p.getDistributionInfo().getVersion().equals(pd.getVersion())) {
              if (stopExistingProcesses) {
                ctx.debug("Existing process will be stopped: " + p);
                toStop.add(p);
              } else {
                ctx.debug("Existing process will not be stopped: " + p);
              }
            } else {
              ctx.debug("Process will be started: " + p);
              toStart.add(pd);
            }
          }
        }
      }
    }
    
    if (!configsToStart.isEmpty()) {
      // no existing processes found
      if (toStop.size() == 0) {
        ctx.info("Did not find old processes");
        if (toStart.size() > 0) {
          ctx.info("Starting process(es)...");
        } else {
          ctx.info("Did not find any process to start");
        }
  
        execNewProcesses(ctx.getTaskManager(), toStart);
      }
      // existing processes found, killing
      else {
        KillListener listener = new KillListener(ctx.getTaskManager(), toStop.size(), toStart);
        for (Process p : toStop) {
          ctx.warn("Found old processes; proceeding to kill");
          ProcessorConfiguration conf = ctx.getServerContext().getServices().getProcessor().getConfiguration();
  
          KillTask kill = new KillTask(p.getMaxKillRetry());
  
          ctx.getTaskManager().executeBackground(kill, TaskParams.createFor(p, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER),
              BackgroundTaskConfig.create(listener).setExecDelay(0).setExecInterval(conf.getKillIntervalMillis()));
        }
      }
    }
    return null;
  }

  private void execNewProcesses(TaskManager tm, Set<ProcessDef> toStart) {
    ExecNewProcessesTask exec = new ExecNewProcessesTask(toStart);
    try {
      tm.execute(exec, null);
    } catch (Throwable err) {
      err.printStackTrace();
    }
  }

  // ////////////////// BackgroundTaskListener interface /////////////////////

  class KillListener implements BackgroundTaskListener {

    private TaskManager tasks;
    private volatile int counter;
    private Set<ProcessDef> toStart;

    public KillListener(TaskManager tasks, int counter, Set<ProcessDef> toStart) {
      this.tasks = tasks;
      this.counter = counter;
      this.toStart = toStart;
    }

    public synchronized void executionAborted(Task<?, ?> task) {
      decrement();
    }

    public synchronized void executionFailed(Task<?, ?> task, Throwable err) {
      decrement();
    }

    public synchronized void executionSucceeded(Task<?, ?> task, Object result) {
      decrement();
    }

    public synchronized void maxExecutionReached(Task<?, ?> task) {
      decrement();
    }

    private void decrement() {
      counter--;
      if (counter <= 0) {
        execNewProcesses(tasks, toStart);
      }
    }
  }

  protected abstract List<ExecConfig> getExecConfigsToStart(TaskExecutionContext ctx) throws Exception;

  protected boolean canExecuteFor(TaskExecutionContext ctx, Distribution d) {
    return true;
  }
}
