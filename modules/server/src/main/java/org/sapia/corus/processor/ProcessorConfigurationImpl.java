package org.sapia.corus.processor;

import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;

public class ProcessorConfigurationImpl implements ProcessorConfiguration {

  static final long serialVersionUID = 1L;

  /**
   * This constant specifies the default "process timeout" - delay (in seconds)
   * after which a process is considered idled and must be restarted.
   */
  public static final int DEFAULT_PROCESS_TIMEOUT = 25;

  /**
   * This constant specifies the default interval (in seconds) at which process
   * status is checked.
   */
  public static final int DEFAULT_CHECK_INTERVAL = 15;

  /**
   * This constant specifies the default interval (in seconds) at which process
   * diagnostic is done as part of process health check.
   */
  public static final int DEFAULT_DIAGNOSTIC_CHECK_INTERVAL = 30;
  
  /**
   * This constant specifies the default interval (in seconds) at which kill
   * attempts occur.
   */
  public static final int DEFAULT_KILL_INTERVAL = 15;

  /**
   * This constant specifies the amount of time (in seconds) to wait between
   * process startups.
   */
  public static final int DEFAULT_START_INTERVAL = 15;

  /**
   * This constant specifies the minimum amount of time (in seconds) required
   * between two startups for the second one to be authorized; value is 120
   * (seconds).
   */
  public static final int DEFAULT_RESTART_INTERVAL = 120;
  
  /**
   * This constant specifies the amount of seconds to wait for prior to lauch the
   * boot time execution configurations.
   */
  public static final int DEFAULT_BOOT_EXEC_DELAY = 30;
  
  public static final int DEFAULT_PROCESS_PUBLISH_INTERVAL = 5;
  
  private int processTimeout = DEFAULT_PROCESS_TIMEOUT;
  private int processCheckInterval = DEFAULT_CHECK_INTERVAL;
  private int processDiagnosticCheckInterval = DEFAULT_DIAGNOSTIC_CHECK_INTERVAL;
  private int killInterval = DEFAULT_KILL_INTERVAL;
  private int startInterval = DEFAULT_START_INTERVAL;
  private int restartInterval = DEFAULT_RESTART_INTERVAL;
  private int bootExecDelay = DEFAULT_BOOT_EXEC_DELAY;
  private OptionalValue<Integer> publishMaxAttempts = OptionalValue.none();
  private int publishInterval = DEFAULT_PROCESS_PUBLISH_INTERVAL;
  private boolean bootExecEnabled = true;
  private boolean autoRestart = true;

  @Override
  public long getProcessTimeoutMillis() {
    return TimeUnit.SECONDS.toMillis(processTimeout);
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
  
  @Override
  public long getProcessDiagnosticCheckIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(processDiagnosticCheckInterval);
  }
  
  public int getProcessDiagnosticCheckInterval() {
    return processDiagnosticCheckInterval;
  }
  
  public void setProcessDiagnosticCheckInterval(int processDiagnosticCheckInterval) {
    this.processDiagnosticCheckInterval = processDiagnosticCheckInterval;
  }

  @Override
  public long getProcessCheckIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(processCheckInterval);
  }

  public int getKillInterval() {
    return killInterval;
  }

  @Override
  public long getKillIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(killInterval);
  }

  public void setKillInterval(int killInterval) {
    this.killInterval = killInterval;
  }

  public int getStartInterval() {
    return startInterval;
  }

  @Override
  public long getStartIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(startInterval);
  }

  public void setStartInterval(int startInterval) {
    this.startInterval = startInterval;
  }

  public int getRestartInterval() {
    return restartInterval;
  }

  @Override
  public long getRestartIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(restartInterval);
  }

  public void setRestartInterval(int restartInterval) {
    this.restartInterval = restartInterval;
  }

  public int getBootExecDelay() {
    return bootExecDelay;
  }

  @Override
  public long getBootExecDelayMillis() {
    return TimeUnit.SECONDS.toMillis(bootExecDelay);
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
  
  public void setProcessPublishingDiagnosticMaxAttempts(int publishMaxAttempts) {
    if (publishMaxAttempts > 0) {
      this.publishMaxAttempts = OptionalValue.of(publishMaxAttempts);
    } else {
      this.publishMaxAttempts = OptionalValue.none();
    }
  }
  
  @Override
  public OptionalValue<Integer> getProcessPublishingDiagnosticMaxAttempts() {
    return publishMaxAttempts;
  }
  
  public void setProcessPublishingDiagnosticInterval(int publishInterval) {
    this.publishInterval = publishInterval;
  }

  @Override
  public long getProcessPublishingDiagnosticIntervalMillis() {
    return TimeUnit.SECONDS.toMillis(publishInterval);
  }

}
