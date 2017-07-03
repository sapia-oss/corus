package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

/**
 * A base class for cluster notifications.
 * 
 * @author yduchesne
 * 
 */
public abstract class ClusterNotification implements Externalizable {

  static final long serialVersionUID = 1L;

  private Set<Endpoint> visited = new HashSet<Endpoint>();
  private Set<Endpoint> targets = new HashSet<Endpoint>();
  private long timestamp = System.currentTimeMillis();

  /**
   * @param ep
   *          the {@link Endpoint} to test.
   * @return <code>true</code> if this instance targets the given endpoint.
   */
  public boolean isTargeted(Endpoint ep) {
    if (targets.isEmpty()) {
      return true;
    } else {
      return targets.contains(ep);
    }
  }

  public long getTimestamp() {
    return timestamp;
  }
  
  /**
   * @return this instance's remote event type.
   */
  public abstract String getEventType();

  /**
   * This method adds the given endpoint to this instance's visited set, and
   * removes it from this instance's target set.
   * 
   * @param ep
   *          the {@link Endpoint} to add to the visited set.
   */
  public void addVisited(Endpoint ep) {
    visited.add(ep);
    targets.remove(ep);
  }

  /**
   * @param target
   *          the {@link Endpoint} of a targeted node.
   */
  public void addTarget(Endpoint target) {
    targets.add(target);
  }

  /**
   * @return this instance's target set.
   */
  public Set<Endpoint> getTargets() {
    return targets;
  }

  /**
   * @return this instance's visited target set.
   */
  public Set<Endpoint> getVisited() {
    return visited;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    visited = (Set<Endpoint>) in.readObject();
    targets = (Set<Endpoint>) in.readObject();
    timestamp = in.readLong();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(visited);
    out.writeObject(targets);
    out.writeLong(timestamp);
  }

}
