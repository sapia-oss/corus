package org.sapia.corus.processor.task;

import java.util.Properties;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * Actually performs the restart of a given {@link Process}.
 * 
 * @author yduchesne
 * 
 */
public class PerformProcessRestartTask extends Task<Boolean, Process> {

  @Override
  public Boolean execute(TaskExecutionContext ctx, Process process) throws Throwable {
    Deployer deployer = ctx.getServerContext().getServices().lookup(Deployer.class);
    Distribution dist;

    ctx.debug("Executing process");
    try {
      DistributionCriteria criteria = DistributionCriteria.builder()
          .name(process.getDistributionInfo().getName())
          .version(process.getDistributionInfo().getVersion()).build();

      dist = deployer.getDistribution(criteria);
    } catch (DistributionNotFoundException e) {
      ctx.error(String.format("Could not find corresponding distribution; process %s will not be restarted", process), e);
      return false;
    }
    
    if (process.getStatus() == LifeCycleStatus.RESTARTING) {
      ProcessConfig processConf = dist.getProcess(process.getDistributionInfo().getProcessName());
      ProcessInfo info = new ProcessInfo(process, dist, processConf, true);
      Properties processProperties = ctx.getServerContext().getProcessProperties(processConf.getPropertyCategories());
      PerformExecProcessTask execProcess = new PerformExecProcessTask();

      try {
        if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, processProperties)).get()) {
          process.touch();
          process.clearCommands();
          process.setStatus(Process.LifeCycleStatus.ACTIVE);
          process.recycle();
          return true;
        } else {
          if (!process.isDeleted()) {
            process.delete();
          }
          return false;
        }
      } catch (Exception e) {
        ctx.error(String.format("Error trying to restart %s; will not be restarted", process), e);
        return false;
      }
    } else {
      try {
        DistributionCriteria criteria = DistributionCriteria.builder()
            .name(process.getDistributionInfo().getName())
            .version(process.getDistributionInfo().getVersion())
            .build();

        dist = deployer.getDistribution(criteria);
      } catch (DistributionNotFoundException e) {
        ctx.error(String.format("Could not find corresponding distribution; process %s will not be restarted", process), e);
        return false;
      } catch (Exception e) {
        ctx.error(String.format("Error trying to restart %s;  will not be restarted", process), e);
        return false;
      }

      ProcessConfig conf = dist.getProcess(process.getDistributionInfo().getProcessName());
      ProcessInfo info = new ProcessInfo(process, dist, conf, true);
      Properties processProperties = ctx.getServerContext().getProcessProperties(conf.getPropertyCategories());

      PerformExecProcessTask execProcess = new PerformExecProcessTask();
      if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, processProperties)).get()) {
        process.touch();
        process.clearCommands();
        process.setStatus(Process.LifeCycleStatus.ACTIVE);
        process.recycle();
        return true;
      } else {
        if (!process.isDeleted()) {
          process.delete();
        }
        return false;
      }
    }
  }

}
