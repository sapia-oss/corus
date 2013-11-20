package org.sapia.corus.processor.task;

import java.util.List;

import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.processor.ExecConfigDatabase;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This tasks starts processes corresponding to {@link ExecConfig} instances,
 * provided that the processes are bootstrappable.
 * <p>
 * It will proceed to killing currently "older" processes (of a different
 * version) if any are running.
 * 
 * @author yduchesne
 * 
 */
public class BootstrapExecConfigStartTask extends AbstractExecConfigStartTask {

  public BootstrapExecConfigStartTask() {
    super(true);
    super.setMaxExecution(1);
  }

  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
    ctx.info("Checking for bootstrap processes; will start if any is found");
    return super.execute(ctx, param);
  }

  @Override
  protected List<ExecConfig> getExecConfigsToStart(TaskExecutionContext ctx) {
    ExecConfigDatabase execConfigs = ctx.getServerContext().getServices().getExecConfigs();
    return execConfigs.getBootstrapConfigs();
  }

}
