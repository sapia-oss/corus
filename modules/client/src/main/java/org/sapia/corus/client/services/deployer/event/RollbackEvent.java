package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched in the context of a rollback.
 * 
 * @author yduchesne
 * 
 */
public class RollbackEvent implements Event {

  /**
   * Indicates if the rollback was done automatically in the context of a deployment,
   * or if it was user-requested.
   * 
   * @author yduchesne
   *
   */
  public enum Type {
    AUTO,
    USER
  }
  
  /**
   * Indicates if the rollback was successful or not.
   * 
   * @author yduchesne
   *
   */
  public enum Status {
    SUCCESS,
    FAILURE
  }

  private Distribution distribution;
  private Type         type;
  private Status       status;

  public RollbackEvent(Distribution dist, Type type, Status status) {
    this.distribution = dist;
    this.type         = type;
    this.status       = status;
  }

  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }
  
  /**
   * @return this instance's {@link Type}.
   */
  public Type getType() {
    return type;
  }
  
  /**
   * @return this instance's status.
   */
  public Status getStatus() {
    return status;
  }

}
