package org.sapia.corus.taskmanager.core;

public class BackgroundTaskConfig extends TaskConfig {

  private long execDelay, execInterval;

  private BackgroundTaskListener listener;

  void setListener(BackgroundTaskListener listener) {
    this.listener = listener;
  }

  public BackgroundTaskListener getListener() {
    return listener;
  }

  public BackgroundTaskConfig setExecDelay(long execDelay) {
    this.execDelay = execDelay;
    return this;
  }

  public long getExecDelay() {
    return execDelay;
  }

  public BackgroundTaskConfig setExecInterval(long interval) {
    this.execInterval = interval;
    return this;
  }

  public long getExecInterval() {
    return execInterval;
  }

  public static BackgroundTaskConfig create() {
    return new BackgroundTaskConfig();
  }

  public static BackgroundTaskConfig create(BackgroundTaskListener listener) {
    BackgroundTaskConfig cfg = create();
    cfg.setListener(listener);
    return cfg;
  }
}
