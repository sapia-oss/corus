package org.sapia.corus.admin.services.processor;

import org.sapia.corus.util.Property;

public interface ProcessorConfiguration {

  public abstract Property getProcessTimeout();

  public abstract long getProcessTimeoutMillis();

  public abstract Property getProcessCheckInterval();

  public abstract long getProcessCheckIntervalMillis();

  public abstract Property getKillInterval();

  public abstract long getKillIntervalMillis();

  public abstract Property getStartInterval();

  public abstract long getStartIntervalMillis();

  public abstract Property getRestartInterval();

  public abstract long getRestartIntervalMillis();

  public abstract Property getExecInterval();

  public abstract long getExecIntervalMillis();

  public abstract Property getBootExecDelay();

  public abstract long getBootExecDelayMillis();

}