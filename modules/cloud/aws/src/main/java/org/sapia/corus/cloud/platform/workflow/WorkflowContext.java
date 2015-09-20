package org.sapia.corus.cloud.platform.workflow;

import org.sapia.corus.cloud.aws.util.TimeSupplier;

/**
 * Encapsulates data contextual to a whole worflow.
 * 
 * @author yduchesne
 *
 */
public class WorkflowContext {
  
  private WorkflowLog  log = DefaultWorkflowLog.getDefault();
  private TimeSupplier timeSupplier = TimeSupplier.SystemTime.getInstance();
  
  public WorkflowLog getLog() {
    return log;
  }
  
  public void setLog(WorkflowLog log) {
    this.log = log;
  }
  
  public TimeSupplier getTimeSupplier() {
    return timeSupplier;
  }
  
  public void setTimeSupplier(TimeSupplier supplier) {
    this.timeSupplier = supplier;
  }
}
