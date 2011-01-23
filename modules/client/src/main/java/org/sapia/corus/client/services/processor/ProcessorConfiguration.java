package org.sapia.corus.client.services.processor;

import java.rmi.Remote;


public interface ProcessorConfiguration extends Remote{

  public long getProcessTimeoutMillis();
  
  public long getProcessCheckIntervalMillis();

  public long getKillIntervalMillis();

  public long getStartIntervalMillis();

  public long getRestartIntervalMillis();

  public long getExecIntervalMillis();

  public long getBootExecDelayMillis();

}