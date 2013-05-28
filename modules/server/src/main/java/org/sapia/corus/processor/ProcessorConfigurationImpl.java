package org.sapia.corus.processor;

import org.sapia.corus.client.services.processor.ProcessorConfiguration;

public class ProcessorConfigurationImpl implements ProcessorConfiguration {
  
  static final long serialVersionUID = 1L;
  
  /**
   * This constant specifies the default "process timeout" - delay (in seconds)
   * after which a process is considered idled and must be restarted.
   */
  public static final int DEFAULT_PROCESS_TIMEOUT = 25;

  /**
   * This constant specifies the default interval (in seconds) at which
   * process status is checked.
   */
  public static final int DEFAULT_CHECK_INTERVAL 	= 15;

  /**
   * This constant specifies the default interval (in seconds) at which
   * kill attempts occur.
   */
  public static final int DEFAULT_KILL_INTERVAL 	= 15;
  
  /**
   * This constant specifies the amount of time (in seconds) to wait
   * between process startups.
   */
  public static final int DEFAULT_START_INTERVAL 	= 15;  

  /**
   * This constant specifies the minimum amount of time (in
   * seconds) required between two startups for the second one
   * to be authorized; value is 120 (seconds).
   */
  public static int DEFAULT_RESTART_INTERVAL   		= 120;
  
  /**
   * This constant specifis the amount of seconds to wait for prior to
   * lauch the boot time execution configurations.
   */
  public static int DEFAULT_BOOT_EXEC_DELAY 			= 30;
  
  private int processTimeout 			 = DEFAULT_PROCESS_TIMEOUT;
  private int processCheckInterval = DEFAULT_CHECK_INTERVAL;
  private int killInterval 				 = DEFAULT_KILL_INTERVAL;
  private int startInterval        = DEFAULT_START_INTERVAL;
  private int restartInterval 		 = DEFAULT_RESTART_INTERVAL;
  private int bootExecDelay 			 = DEFAULT_BOOT_EXEC_DELAY;
  private boolean bootExecEnabled  = true;
  private boolean autoRestart      = true;
  
  
  public long getProcessTimeoutMillis(){
    return processTimeout * 1000;
  }
  
  public void setProcessTimeout(int processTimeout) {
    this.processTimeout = processTimeout;
  }
  
  public int getProcessCheckInterval() {
    return processCheckInterval;
  }

  public void setProcessCheckInterval(int processCheckInterval) {
    this.processCheckInterval = processCheckInterval;
  }

  public long getProcessCheckIntervalMillis(){
    return processCheckInterval * 1000;
  }
  
  public int getKillInterval() {
    return killInterval;
  }
  
  public long getKillIntervalMillis(){
    return killInterval * 1000;
  }

  public void setKillInterval(int killInterval) {
    this.killInterval = killInterval;
  }
  
  public int getStartInterval() {
    return startInterval;
  }

  public long getStartIntervalMillis(){
    return this.startInterval * 1000;
  }

  public void setStartInterval(int startInterval) {
    this.startInterval = startInterval;
  }
  
  public int getRestartInterval() {
    return restartInterval;
  }

  public long getRestartIntervalMillis(){
    return this.restartInterval * 1000;
  }
  
  public void setRestartInterval(int restartInterval) {
    this.restartInterval = restartInterval;
  }
  
  public int getBootExecDelay() {
    return bootExecDelay;
  }
  
  public long getBootExecDelayMillis() {
    return bootExecDelay * 1000;
  }

  public void setBootExecDelay(int bootExecDelay) {
    this.bootExecDelay = bootExecDelay;
  }
  
  public void setAutoRestart(boolean autoRestart) {
    this.autoRestart = autoRestart;
  }
  
  public boolean autoRestartStaleProcesses() {
    return autoRestart;
  }

  public void setBootExecEnabled(boolean bootExecEnabled) {
    this.bootExecEnabled = bootExecEnabled;
  }
  
  public boolean isBootExecEnabled() {
    return bootExecEnabled;
  }
}
