package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.common.Mappable;

/**
 * Encapsulates the status in the cluster for a given Corus host.
 * 
 * @author yduchesne
 * 
 */
public class ClusterStatus implements Externalizable, Mappable {

  static final long serialVersionUID = 1L;

  private CorusHost host;
  private int nodeCount;
  
  /** DO NOT USE: meant for externalization only. */
  public ClusterStatus() {
  }

  public ClusterStatus(CorusHost host, int nodeCount) {
    this.host      = host;
    this.nodeCount = nodeCount;
  }

  /**
   * @return the Corus host information.
   */
  public CorusHost getHost() {
    return host;
  }
  
  /**
   * @return the number of nodes in the cluster.
   */
  public int getNodeCount() {
    return nodeCount;
  }
  
  // --------------------------------------------------------------------------
  // Mappable interface
  
  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("host.formattedAddress", host.getFormattedAddress());
    toReturn.put("host.address", host.getEndpoint().getServerTcpAddress().getHost());
    toReturn.put("host.port", host.getEndpoint().getServerTcpAddress().getPort());
    toReturn.put("host.nodeCount", nodeCount);
    return toReturn;
  }

  // --------------------------------------------------------------------------
  // Externalizable interface

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    host = (CorusHost) in.readObject();
    nodeCount = in.readInt();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(host);
    out.writeInt(nodeCount);
  }
}
