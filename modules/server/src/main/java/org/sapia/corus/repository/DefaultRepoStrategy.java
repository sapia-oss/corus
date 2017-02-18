package org.sapia.corus.repository;

import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

public class DefaultRepoStrategy implements RepoStrategy {
  
  private RepoRole role;
  
  public DefaultRepoStrategy(RepoRole role) {
    this.role = role;
  }
  
  @Override
  public boolean acceptsEvent(RepoEventType eventType) {
    return role == eventType.destType();
  }
  
  @Override
  public boolean acceptsPull() {
    return role == RepoRole.CLIENT;
  }

}
