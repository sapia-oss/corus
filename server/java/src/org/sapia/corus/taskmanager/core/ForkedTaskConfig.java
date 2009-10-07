package org.sapia.corus.taskmanager.core;

public class ForkedTaskConfig extends TaskConfig{
  
  private TaskListener listener;

  void setListener(TaskListener listener) {
    this.listener = listener;
  }
  
  public TaskListener getListener() {
    return listener;
  }
  
  public static ForkedTaskConfig create(){
    return new ForkedTaskConfig();
  }
  
  public static ForkedTaskConfig create(TaskListener listener){
    ForkedTaskConfig cfg = create();
    cfg.setListener(listener);
    return cfg;
  }

}
