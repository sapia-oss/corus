package org.sapia.corus.processor.task;

import java.io.IOException;
import java.util.Properties;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * This task resumes a suspended process.
 * 
 * @author Yanick Duchesne
 */
public class ResumeTask extends Task<Void, TaskParams<Process, Distribution, ProcessConfig, Void>> {

  private LockOwner lockOwner = new LockOwner();

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<Process, Distribution, ProcessConfig, Void> params) throws Throwable {

    Process process = params.getParam1();
    Distribution dist = params.getParam2();
    ProcessConfig conf = params.getParam3();

    process.getLock().acquire(lockOwner);

    try {
      PerformExecProcessTask execProcess = new PerformExecProcessTask();
      ProcessInfo info = new ProcessInfo(process, dist, conf, true);
      Properties props = ctx.getServerContext().getProcessProperties(conf.getPropertyCategories());
      if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, props)).get()) {
        process.setStatus(Process.LifeCycleStatus.ACTIVE);
        process.save();
      } else {
        ctx.error(String.format("Process: %s will not be resumed", process));
      }
    } catch (IOException e) {
      ctx.error(String.format("Error restarting process: %s", process), e);
    } finally {
      process.getLock().release(lockOwner);
    }
    return null;
  }
}
