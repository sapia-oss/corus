package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.ubik.util.Strings;

/**
 * Holds {@link PortRange} instances.
 * 
 * @author yduchesne
 * 
 */
public class PortRangeNotification extends ClusterNotification {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.port-range";

  private List<PortRange> portRanges = new ArrayList<PortRange>();
  private boolean         force;

  /** Do not use: meant for serialization only */
  public PortRangeNotification() {
  }

  /**
   * @param configs
   *          a {@link List} of {@link PortRange} instances.
   */
  public PortRangeNotification(List<PortRange> portRanges) {
    this.portRanges = portRanges;
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  /**
   * @return this instance's {@link List} of {@link PortRange}s.
   */
  public List<PortRange> getPortRanges() {
    return portRanges;
  }
  
  /**
   * Sets this instance's <code>force</code>.
   * 
   */
  public PortRangeNotification setForce(boolean force) {
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

  // --------------------------------------------------------------------------
  // Externalizable

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    portRanges = (List<PortRange>) in.readObject();
    force      = in.readBoolean();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(portRanges);
    out.writeBoolean(force);
  }

  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public String toString() {
    return Strings.toStringFor(this, "portRanges", portRanges, "targeted", getTargets(), "visited", getVisited());
  }

}
