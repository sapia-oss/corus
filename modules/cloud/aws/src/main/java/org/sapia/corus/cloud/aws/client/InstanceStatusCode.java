package org.sapia.corus.cloud.aws.client;

public enum InstanceStatusCode {
  
  PENDING(0),
  RUNNING(16),
  TERMINATED(48),
  STOPPED(80);
  
  private int value;

  private InstanceStatusCode(int value) {
    this.value = value;
  }
  
  public int value() {
    return value;
  }
}