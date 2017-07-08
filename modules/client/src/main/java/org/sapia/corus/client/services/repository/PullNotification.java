package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * An instance of this class is sent to Corus nodes, in order 
 * to trigger a repository pull.
 * 
 * @author yduchesne
 * 
 */
public class PullNotification extends ClusterNotification {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.pull";

  private boolean                 force;
  private OptionalValue<Endpoint> source = OptionalValue.none();
  
  /**
   * Do not call: used for externalization only.
   */
  public PullNotification() {
    
  }
  
  /**
   * Sets this instance's <code>force</code> flag.
   * 
   * @return this instance.
   */
  public PullNotification setForce(boolean force) {
    this.force = force;
    return this;
  }
  
  /**
   * @return <code>true</code> if the deployment should be performed whether the 
   * node receiving this request is a repo node or not, and regardless if it has
   * its corresponding "push" flag turn off.
   */
  public boolean isForce() {
    return force;
  }
  
  /**
   * @param ep the {@link Endpoint} corresponding to the Corus node that initiated
   * this notification.
   * @return this instance.
   */
  public PullNotification setSource(Endpoint ep) {
    source = OptionalValue.of(ep);
    return this;
  }
  
  /**
   * @return this instance's optional source {@link Endpoint}.
   * @see #setSource(Endpoint)
   */
  public OptionalValue<Endpoint> getSource() {
    return source;
  }
  
  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    force  = in.readBoolean();
    source = (OptionalValue<Endpoint>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeBoolean(force);
    out.writeObject(source);
  }

}
