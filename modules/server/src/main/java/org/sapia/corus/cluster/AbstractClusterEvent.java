package org.sapia.corus.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.util.Strings;

/**
 * The abstract class for Corus clustering events.
 * 
 * @author yduchesne
 * 
 */
public class AbstractClusterEvent implements Externalizable {

  private CorusHost origin;

  /**
   * Meant for externalization
   */
  public AbstractClusterEvent() {
  }

  /**
   * @param origin
   *          the {@link CorusHost} of the node from which this event
   *          originates.
   */
  public AbstractClusterEvent(CorusHost origin) {
    this.origin = origin;
  }

  /**
   * @return the {@link CorusHost} of the node from which this event originates.
   */
  public CorusHost getOrigin() {
    return origin;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "origin", origin);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    origin = (CorusHost) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(origin);
  }

}
