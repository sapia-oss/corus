package org.sapia.corus.processor.task;

import java.util.Properties;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
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
 * @author Yanick Duchesne
 */
public class ExecTask extends Task<Void, TaskParams<Distribution, ProcessConfig, String, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return ProcessorThrottleKeys.PROCESS_EXEC;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<Distribution, ProcessConfig, String, Void> params) throws Throwable {

    Distribution dist = params.getParam1();
    ProcessConfig conf = params.getParam2();
    String profile = params.getParam3();

    Process process = new Process(new DistributionInfo(dist.getName(), dist.getVersion(), profile, conf.getName()));

    ProcessInfo info = new ProcessInfo(process, dist, conf, false);
    PerformExecProcessTask execProcess = new PerformExecProcessTask();
    Properties processProperties = ctx.getServerContext().getProcessProperties();

    ctx.info(String.format("Executing process: %s", process));
    if (ctx.getTaskManager().executeAndWait(execProcess, TaskParams.createFor(info, processProperties)).get()) {
      ctx.info(String.format("Added process to active process list: %s", process));
      ctx.getServerContext().getServices().getProcesses().getActiveProcesses().addProcess(process);
    } else {
      ctx.info("Process execution failed; no process representation internally kept");
    }
    return null;
  }
}
