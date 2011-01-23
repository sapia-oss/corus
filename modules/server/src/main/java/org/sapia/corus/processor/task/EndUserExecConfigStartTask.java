package org.sapia.corus.processor.task;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.processor.ExecConfigDatabase;
import org.sapia.corus.processor.StartupLock;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Starts processes corresponding to an {@link ExecConfig}, as request by an
 * end-user.
 * 
 * @author yduchesne
 *
 */
public class EndUserExecConfigStartTask extends AbstractExecConfigStartTask{

  private String execConfigName;
  
  public EndUserExecConfigStartTask(String execConfigName, StartupLock lock) {
    super(lock, false);
    this.execConfigName = execConfigName;
  }
  
  @Override
  protected List<ExecConfig> getExecConfigsToStart(TaskExecutionContext ctx) throws Exception{
    ExecConfigDatabase execConfigs = ctx.getServerContext().getServices().getExecConfigs();
    ExecConfig config = execConfigs.getConfigFor(execConfigName);
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    toReturn.add(config);
    return toReturn;
  }
}
