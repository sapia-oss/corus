package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;


public class DeploymentEvent implements Event{

  private Distribution distribution;
  
  public DeploymentEvent(Distribution dist) {
    this.distribution = dist;
  }
  
  public Distribution getDistribution() {
    return distribution;
  }
  
}
