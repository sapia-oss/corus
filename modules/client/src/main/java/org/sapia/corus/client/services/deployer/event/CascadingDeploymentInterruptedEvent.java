package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.ubik.net.ServerAddress;

import java.util.Collections;
import java.util.Set;

/**
 * Dispatched when a cascading deployment has been interrupted.
 * 
 * @author yduchesne
 *
 */
public class CascadingDeploymentInterruptedEvent extends CorusEventSupport {
  
  private ServerAddress      currentHost;
  private Set<ServerAddress> remainingHosts;
  
  public CascadingDeploymentInterruptedEvent(ServerAddress currentHost, Set<ServerAddress> remainingHosts) {
    this.currentHost    = currentHost;
    this.remainingHosts = remainingHosts;
  }

  /**
   * @return the {@link ServerAddress} corresponding to the Corus node at which the deployment 
   *         was interrupted.
   */
  public ServerAddress getCurrentHost() {
    return currentHost;
  }
  
  /**
   * @return the {@link Set} of {@link ServerAddress} instances corresponding to the Corus nodes
   *         that were targeted by a deployment but to which the deployment failed.
   */
  public Set<ServerAddress> getRemainingHosts() {
    return Collections.unmodifiableSet(remainingHosts);
  }
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.ERROR;
  }
  
  @Override
  public EventLog toEventLog() {
    return EventLog.builder()
        .source(source())
        .level(getLevel())
        .type(type())
        .message("Cascading deployment interrupted. ")
        .message("Host received deployment but could ")
        .message("not connect to next host in chain. ")
        .message("Got %s hosts remaining to deploy to", remainingHosts.size())
        .build();
  }
  
  @Override
  protected Class<?> source() {
    return Deployer.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("remainingHostCount").value(remainingHosts.size());
  }

}
