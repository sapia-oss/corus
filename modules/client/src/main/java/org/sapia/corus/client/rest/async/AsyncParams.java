package org.sapia.corus.client.rest.async;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.facade.CorusConnector;

public class AsyncParams {

  private CorusConnector  connector;
  private ClusterInfo     clusterInfo;
  
  public AsyncParams(CorusConnector connector, ClusterInfo clusterInfo) {
    this.connector  = connector;
    this.clusterInfo = clusterInfo;
  }
  
  public CorusConnector getConnector() {
    return connector;
  }
  
  public ClusterInfo getClusterInfo() {
    return clusterInfo;
  }
}