package org.sapia.corus.processor.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.client.services.processor.event.ProcessStartedEvent;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.ProcessorThrottleKeys;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * This task initiates the execution of a new process, based on a given
 * distribution, process configuration, and profile. This task creates a new
 * {@link Process} instance corresponding to the OS process to start. If the
 * execution of the process is successful at the OS level, the {@link Process}
 * instance is saved to the Corus server's database.
 * <p>
 * Calls:
 * <ol>
 * <li> {@link PerformExecProcessTask}: in order to actually start the OS
 * process.
 * </ol>
 * 
 * @author yduchesne
 */
public class ExecTask extends Task<Void, TaskParams<Distribution, ProcessConfig, String, ProcessStartupInfo>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return ProcessorThrottleKeys.PROCESS_EXEC;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<Distribution, ProcessConfig, String, ProcessStartupInfo> params) throws Throwable {

    Distribution       dist        = params.getParam1();
    ProcessConfig      conf        = params.getParam2();
    String             profile     = params.getParam3();
    ProcessStartupInfo startupInfo = params.getParam4();
    RepositoryConfiguration repoConf = ctx.getServerContext().lookup(RepositoryConfiguration.class);

    if (canExecute(ctx, repoConf)) {
      Process process = new Process(new DistributionInfo(dist.getName(), dist.getVersion(), profile, conf.getName()));
      process.setStartupInfo(startupInfo);
      
      ProcessInfo info = new ProcessInfo(process, dist, conf, false);
      PerformExecProcessTask execProcess = new PerformExecProcessTask();
      List<String> categories = new ArrayList<>(dist.getPropertyCategories());
      for (String c : conf.getPropertyCategories()) {
        if (!categories.contains(c)) {
          categories.add(c);
        }
      }
      Properties processProperties = ctx.getServerContext().getProcessProperties(categories);
  
      ctx.info(String.format("Executing process: %s", process));
      if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, processProperties)).get()) {
        ctx.info(String.format("Added process to active process list: %s", process));
        ctx.getServerContext().getServices().getProcesses().addProcess(process);
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessStartedEvent(dist, conf, process));
      } else {
        ctx.info("Process execution failed; no process representation internally kept");
      }
    } else {
      ctx.warn("Process execution ignored: this Corus server is in repo server mode and configured with process executiond disabled");
    }
    return null;
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private boolean canExecute(TaskExecutionContext ctx, RepositoryConfiguration conf) {
    if (ctx.getServerContext().getCorusHost().getRepoRole() == RepoRole.SERVER) {
      return conf.isRepoServerExecProcessEnabled();
    }
    return true;
  }
}
