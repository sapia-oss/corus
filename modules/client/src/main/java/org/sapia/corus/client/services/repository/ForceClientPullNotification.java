package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * An instance of this class is sent to repository clients, in order to force
 * them to perform repository pull.
 * 
 * @author yduchesne
 *
 */
public class ForceClientPullNotification extends ClusterNotification {
  
  static final long serialVersionID = 1L;
  
  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.force-client-pull";
  
  private Endpoint repoEndpoint;
  
  public ForceClientPullNotification(Endpoint repoEndpoint) {
    this.repoEndpoint = repoEndpoint;
  }
  
  /**
   * @return the {@link Endpoint} corresponding to the repository that sent this notification.
   */
  public Endpoint getRepoEndpoint() {
    return repoEndpoint;
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
  }
  
}
