package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

/**
 * An instance of this class is sent to repository clients, in order to force
 * them to perform repository pull.
 * 
 * @author yduchesne
 * 
 */
public class ChangeRepoRoleNotification extends ClusterNotification {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.change-role";

  private Endpoint repoEndpoint;
  private RepoRole newRole;

  public ChangeRepoRoleNotification(Endpoint repoEndpoint, RepoRole newRole) {
    this.repoEndpoint = repoEndpoint;
    this.newRole      = newRole;
  }

  /**
   * @return the {@link Endpoint} corresponding to the repository that sent this
   *         notification.
   */
  public Endpoint getRepoEndpoint() {
    return repoEndpoint;
  }
  
  /**
   * @return the new {@link RepoRole} of the instance that sent this notification.
   */
  public RepoRole getNewRole() {
    return newRole;
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    newRole = (RepoRole) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(newRole);
  }

}
