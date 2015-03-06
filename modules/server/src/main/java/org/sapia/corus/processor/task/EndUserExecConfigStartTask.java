package org.sapia.corus.processor.task;

import java.util.List;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.processor.ExecConfigDatabase;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Starts processes corresponding to an {@link ExecConfig}, as request by an
 * end-user.
 * 
 * @author yduchesne
 * 
 */
public class EndUserExecConfigStartTask extends AbstractExecConfigStartTask {

  private ExecConfigCriteria criteria;

  public EndUserExecConfigStartTask(ExecConfigCriteria criteria) {
    super(false);
    this.criteria = criteria;
  }

  @Override
  protected List<ExecConfig> getExecConfigsToStart(TaskExecutionContext ctx) throws Exception {
    ExecConfigDatabase execConfigs = ctx.getServerContext().getServices().getExecConfigs();
    return execConfigs.getConfigsFor(criteria);
  }
  
  @Override
  protected boolean canExecuteFor(TaskExecutionContext ctx, Distribution d) {
    return true;
  }
}
