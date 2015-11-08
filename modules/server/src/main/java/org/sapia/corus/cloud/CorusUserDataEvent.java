package org.sapia.corus.cloud;

import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Encapsulates a {@link CorusUserData}.
 * 
 * @author yduchesne
 *
 */
public class CorusUserDataEvent implements Event {
  
  private CorusUserData userData;
  
  public CorusUserDataEvent(CorusUserData userData) {
    this.userData = userData;
  }
  
  /**
   * @return the {@link CorusUserData} instance for which this event is dispatched.
   */
  public CorusUserData getUserData() {
    return userData;
  }

}
