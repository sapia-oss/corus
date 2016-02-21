package org.sapia.corus.client.services.processor;

import java.rmi.Remote;

import org.sapia.corus.client.common.OptionalValue;

/**
 * Specifies the {@link Processor}'s configuration.
 * 
 * @author yduchesne
 *
 */
public interface ProcessorConfiguration extends Remote {

  /**
   * Returns the delay after which processes that have not polled their Corus
   * server are considered "timed out".
   * 
   * @return the process timeout, in millis.
   */
  public long getProcessTimeoutMillis();

  /**
   * Returns the interval at which the Corus server checks for timed out
   * processes.
   * 
   * @return the process check interval, in millis.
   */
  public long getProcessCheckIntervalMillis();
  
  
  /**
   * Returns the interval at which the Corus server does diagnostic checks on on processes,
   * as part of the timeout check.
   * 
   * @return the process diagnostic check interval, in millis.
   */
  public long getProcessDiagnosticCheckIntervalMillis();

  /**
   * Returns the interval between process kill attempts.
   * 
   * @return the process kill interval, in millis.
   */
  public long getKillIntervalMillis();

  /**
   * Returns the amount of time to wait for between process startups
   * 
   * @return the process start interval, in millis.
   */
  public long getStartIntervalMillis();

  /**
   * Amount of time a process must have been running for before it crashed and
   * in order for an automatic restart to be authorized.
   * 
   * @return process restart interval, in millis.
   */
  public long getRestartIntervalMillis();

  /**
   * The amount of time to wait for (in millis) before automatically starting
   * processes after Corus startup.
   * 
   * @return the boot process execution delay.
   */
  public long getBootExecDelayMillis();

  /**
   * @return <code>true</code> if Corus should auto-restart stale processes.
   */
  public boolean autoRestartStaleProcesses();

  /**
   * @return <code>true</code> if process startup at Corus boot time is enabled.
   */
  public boolean isBootExecEnabled();
  
  /**
   * @return the maximum number of times that the process publishing background task should attempt
   * trying to acquire pre-publish process diagnostic - or returns {@link OptionalValue#none()} if
   * no maximum number of attempts were specified.
   */
  public OptionalValue<Integer> getProcessPublishingDiagnosticMaxAttempts();
  
  /**
   * @return the interval to pause in between diagnostic acquisition attempts by the
   * process publishing background task.
   */
  public long getProcessPublishingDiagnosticIntervalMillis();
}