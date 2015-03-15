package org.sapia.corus.processor.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.StringArg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.processor.ProcessDependencyFilter;
import org.sapia.corus.processor.ProcessRef;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.log.ProgressQueueTaskLog;

/**
 * Executes the processes corresponding to given {@link ProcessDef}s.
 * 
 * @author yduchesne
 * 
 */
public class ExecNewProcessesTask extends Task<Void, Void> {

  private Set<ProcessDef> toStart;

  public ExecNewProcessesTask(Set<ProcessDef> toStart) {
    this.toStart = toStart;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {

    if (toStart.size() == 0) {
      ctx.info("No processes to start, aborting");
      return null;
    }

    Deployer deployer = ctx.getServerContext().getServices().getDeployer();
    Processor processor = ctx.getServerContext().getServices().getProcessor();
    ProcessRepository processes = ctx.getServerContext().lookup(ProcessRepository.class);
    ProcessDependencyFilter filter = new ProcessDependencyFilter(new ProgressQueueTaskLog(this, ctx.getLog()));

    for (ProcessDef pd : toStart) {
      Distribution dist = null;
      ArgMatcher distName = new StringArg(pd.getDist());
      ArgMatcher version = new StringArg(pd.getVersion());
      ArgMatcher processName = new StringArg(pd.getName());

      ProcessCriteria criteria = ProcessCriteria.builder()
          .distribution(distName)
          .version(version)
          .name(processName)
          .lifecycles(LifeCycleStatus.ACTIVE, LifeCycleStatus.RESTARTING)
          .profile(pd.getProfile()).build();

      List<Process> activeProcesses = processes.getProcesses(criteria);
      if (activeProcesses.size() < pd.getInstances()) {
        try {
          DistributionCriteria distCriteria = DistributionCriteria.builder().name(distName).version(version).build();
          dist = deployer.getDistribution(distCriteria);
        } catch (DistributionNotFoundException e) {
          ctx.warn("Could not acquire distribution", e);
          // noop;
        }
        if (dist != null) {
          for (ProcessConfig conf : dist.getProcesses(processName)) {
            if (conf.containsProfile(pd.getProfile())) {
              filter.addRootProcess(dist, conf, pd.getProfile(), pd.getInstances());
            } else {
              ctx.warn("No profile " + pd.getProfile() + " found for " + pd.getProfile());
              ctx.warn("Got profiles " + conf.getProfiles());
            }
          }
        } else {
          ctx.warn("No distribution found for " + pd);
        }
      } else {
        ctx.warn("Process already started for: " + pd);
      }
    }

    filter.filterDependencies(deployer, processor);

    List<ProcessRef> filteredProcesses = filter.getFilteredProcesses();
    if (filteredProcesses.size() > 0) {

      ctx.info("Dependencies have been resolved; will start the following processes");
      for (ProcessRef ref : filteredProcesses) {
        ctx.info(ref.toString());
      }

      ctx.getTaskManager().execute(new MultiExecTask(), filteredProcesses);
    } else {
      ctx.error("No processes found to execute");
    }
    return null;
  }
}
