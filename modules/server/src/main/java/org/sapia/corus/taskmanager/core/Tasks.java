package org.sapia.corus.taskmanager.core;

public class Tasks {
  
  public static SequentialTaskConfig createSequentialTaskConfig(){
    return new SequentialTaskConfig();
  }

  public static SequentialTaskConfig createSequentialTaskConfig(TaskListener listener){
    SequentialTaskConfig c = createSequentialTaskConfig();
    c.setListener(listener);
    return c;
  }
  
  public static BackgroundTaskConfig createBackgroundTaskConfig(){
    return new BackgroundTaskConfig();
  }

  public static BackgroundTaskConfig createBackgroundTaskConfig(BackgroundTaskListener listener){
    BackgroundTaskConfig cfg = new BackgroundTaskConfig();
    cfg.setListener(listener);
    return cfg;
  }

}
