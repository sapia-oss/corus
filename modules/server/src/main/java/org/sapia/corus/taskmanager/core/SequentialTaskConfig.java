package org.sapia.corus.taskmanager.core;

public class SequentialTaskConfig extends TaskConfig {
  
  private TaskListener listener;
  private int maxExecution;
  
  void setListener(TaskListener listener) {
    this.listener = listener;
  }
  
  public TaskListener getListener() {
    return listener;
  }
  
  public SequentialTaskConfig setMaxExecution(int maxExecution) {
    this.maxExecution = maxExecution;
    return this;
  }
  
  public int getMaxExecution() {
    return maxExecution;
  }
  
  public static SequentialTaskConfig create(){
    return new SequentialTaskConfig();
  }
  
  public static SequentialTaskConfig create(TaskListener listener){
    SequentialTaskConfig cfg = create();
    cfg.setListener(listener);
    return cfg;
  }
}
