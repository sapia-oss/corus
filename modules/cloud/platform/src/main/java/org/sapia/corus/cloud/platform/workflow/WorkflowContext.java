package org.sapia.corus.cloud.platform.workflow;

import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.util.TimeSupplier;

/**
 * Encapsulates data contextual to a whole workflow.
 * 
 * @author yduchesne
 *
 */
public class WorkflowContext {
  
  private WorkflowLog  log = DefaultWorkflowLog.getDefault();
  private TimeSupplier timeSupplier = TimeSupplier.SystemTime.getInstance();
  private Settings     settings;
  
  public WorkflowContext(Settings settings) {
    this.settings = settings;
  }
  
  public Settings getSettings() {
    return settings;
  }
  
  public WorkflowLog getLog() {
    return log;
  }
  
  public WorkflowContext withLog(WorkflowLog log) {
    this.log = log;
    return this;
  }
  
  public TimeSupplier getTimeSupplier() {
    return timeSupplier;
  }
  
  public WorkflowContext withTimeSupplier(TimeSupplier supplier) {
    this.timeSupplier = supplier;
    return this;
  }
}
