package org.sapia.corus.processor.task;

import java.io.IOException;
import java.util.Properties;

import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.event.ProcessRestartedEvent;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * This task restarts a given process (it terminates the process and restarts it
 * immediately).
 * 
 * @author Yanick Duchesne
 */
public class RestartTask extends KillTask {

  public RestartTask(int maxRetry) {
    super(maxRetry);
  }

  @Override
  protected void doKillConfirmed(boolean performOsKill, TaskExecutionContext ctx) {
    try {

      try {
        if (performOsKill && proc.getOsPid() != null) {
          ProcessHookManager processHooks = ctx.getServerContext().lookup(ProcessHookManager.class);
          processHooks.kill(new ProcessContext(proc), KillSignal.SIGKILL, new TaskLogCallback(ctx));
        }
      } catch (IOException e) {
        ctx.warn("Error caught trying to kill process", e);
      }

      DistributionDatabase dists = ctx.getServerContext().getServices().getDistributions();

      DistributionCriteria criteria = DistributionCriteria.builder()
          .name(proc.getDistributionInfo().getName())
          .version(proc.getDistributionInfo().getVersion()).build();

      Distribution  dist = dists.getDistribution(criteria);
      ProcessConfig conf = dist.getProcess(proc.getDistributionInfo().getProcessName());

      synchronized (dists) {
        proc.setStatus(Process.LifeCycleStatus.RESTARTING);
        proc.clearCommands();
        proc.save();
      }

      ctx.warn(String.format("Process %s will be restarted... ", proc));

      synchronized (proc) {
        try {
          ProcessInfo info = new ProcessInfo(proc, dist, conf, true);
          Properties processProperties = ctx.getServerContext().getProcessProperties(conf.getPropertyCategories());
          PerformExecProcessTask execProcess = new PerformExecProcessTask();
          if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, processProperties)).get()) {
            proc.setStatus(LifeCycleStatus.ACTIVE);
            proc.clear();
            proc.save();
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessRestartedEvent(dist, conf, proc));
          } else {
            proc.delete();
          }
        } catch (IOException e) {
          ctx.error(String.format("Could not restart: %s", conf.getName()), e);
        }
      }
    } catch (Exception e) {
      ctx.error(e);
    } finally {
      abort(ctx);
    }
  }
}
