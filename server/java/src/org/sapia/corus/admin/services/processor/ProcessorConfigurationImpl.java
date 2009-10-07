package org.sapia.corus.admin.services.processor;

import java.io.Serializable;

import org.sapia.corus.util.IntProperty;
import org.sapia.corus.util.Property;

public class ProcessorConfigurationImpl implements Serializable, ProcessorConfiguration {
  
  static final long serialVersionUID = 1L;
  
  /**
   * This constant specifies the default "process timeout" - delay (in seconds)
   * after which a process is considered idled and must be restarted.
   */
  public static final Property DEFAULT_PROCESS_TIMEOUT = new IntProperty(25);

  /**
   * This constant specifies the default interval (in seconds) at which
   * process status is checked.
   */
  public static final Property DEFAULT_CHECK_INTERVAL = new IntProperty(15);

  /**
   * This constant specifies the default interval (in seconds) at which
   * kill attempts occur.
   */
  public static final Property DEFAULT_KILL_INTERVAL = new IntProperty(15);
  
  /**
   * This constant specifies the amount of time (in seconds) to wait
   * between process startups.
   */
  public static final Property DEFAULT_START_INTERVAL = new IntProperty(15);  

  /**
   * This constant specifies the minimum amount of time (in
   * seconds) required between two startups for the second one
   * to be authorized; value is 120 (seconds).
   */
  public static final Property DEFAULT_RESTART_INTERVAL   = new IntProperty(120);
  
  public static final Property DEFAULT_EXEC_TASK_INTERVAL = new IntProperty(10);

  public static final Property DEFAULT_BOOT_EXEC_DELAY = new IntProperty(30);

  
  private Property processTimeout = DEFAULT_PROCESS_TIMEOUT;
  private Property processCheckInterval = DEFAULT_CHECK_INTERVAL;
  private Property killInterval = DEFAULT_KILL_INTERVAL;
  private Property startInterval = DEFAULT_START_INTERVAL;
  private Property restartInterval = DEFAULT_RESTART_INTERVAL;
  private Property execInterval = DEFAULT_EXEC_TASK_INTERVAL;
  private Property bootExecDelay = DEFAULT_BOOT_EXEC_DELAY;
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getProcessTimeout()
   */
  public Property getProcessTimeout() {
    return processTimeout;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getProcessTimeoutMillis()
   */
  public long getProcessTimeoutMillis(){
    return processTimeout.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setProcessTimeout(org.sapia.corus.util.Property)
   */
  public void setProcessTimeout(Property processTimeout) {
    this.processTimeout = processTimeout;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getProcessCheckInterval()
   */
  public Property getProcessCheckInterval() {
    return processCheckInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setProcessCheckInterval(org.sapia.corus.util.Property)
   */
  public void setProcessCheckInterval(Property processCheckInterval) {
    this.processCheckInterval = processCheckInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getProcessCheckIntervalMillis()
   */
  public long getProcessCheckIntervalMillis(){
    return processCheckInterval.getLongValue() * 1000;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getKillInterval()
   */
  public Property getKillInterval() {
    return killInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getKillIntervalMillis()
   */
  public long getKillIntervalMillis(){
    return killInterval.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setKillInterval(org.sapia.corus.util.Property)
   */
  public void setKillInterval(Property killInterval) {
    this.killInterval = killInterval;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getStartInterval()
   */
  public Property getStartInterval() {
    return startInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getStartIntervalMillis()
   */
  public long getStartIntervalMillis(){
    return this.startInterval.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setStartInterval(org.sapia.corus.util.Property)
   */
  public void setStartInterval(Property startInterval) {
    this.startInterval = startInterval;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getRestartInterval()
   */
  public Property getRestartInterval() {
    return restartInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getRestartIntervalMillis()
   */
  public long getRestartIntervalMillis(){
    return this.restartInterval.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setRestartInterval(org.sapia.corus.util.Property)
   */
  public void setRestartInterval(Property restartInterval) {
    this.restartInterval = restartInterval;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getExecInterval()
   */
  public Property getExecInterval() {
    return execInterval;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getExecIntervalMillis()
   */
  public long getExecIntervalMillis(){
    return execInterval.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setExecInterval(org.sapia.corus.util.Property)
   */
  public void setExecInterval(Property execInterval) {
    this.execInterval = execInterval;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getBootExecDelay()
   */
  public Property getBootExecDelay() {
    return bootExecDelay;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#getBootExecDelayMillis()
   */
  public long getBootExecDelayMillis() {
    return bootExecDelay.getLongValue() * 1000;
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.processor.ProcessConfigurationIF#setBootExecDelay(org.sapia.corus.util.Property)
   */
  public void setBootExecDelay(Property bootExecDelay) {
    this.bootExecDelay = bootExecDelay;
  }
  
}
