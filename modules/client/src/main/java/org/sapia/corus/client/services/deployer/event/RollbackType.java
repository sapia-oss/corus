package org.sapia.corus.client.services.deployer.event;

/**
 * Indicates if the rollback was done automatically in the context of a deployment,
 * or if it was user-requested.
 * 
 * @author yduchesne
 *
 */
public enum RollbackType {
  AUTO,
  USER
}