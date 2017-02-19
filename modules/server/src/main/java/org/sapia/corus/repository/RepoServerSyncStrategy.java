package org.sapia.corus.repository;

import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

/**
 * A strategy that is used in the context of repo servers that also
 * act as repo clients in order to be synchronized by their repo server peers.
 * <p>
 * This is to act as a failover mechanism when repo servers appear in the network
 * and have nothing on them. In this case, it is desirable that they find existing
 * repo servers to synchronize with them.
 * 
 * @author yduchesne
 *
 */
public class RepoServerSyncStrategy implements RepoStrategy {
  
  private RepoRole role;
  
  public RepoServerSyncStrategy(RepoRole role) {
    this.role = role;
  }
  
  @Override
  public boolean acceptsEvent(RepoEventType eventType) {
    return role == RepoRole.CLIENT || role == RepoRole.SERVER;
  }

  @Override
  public boolean acceptsPull() {
    return role == RepoRole.CLIENT || role == RepoRole.SERVER;
  }
}
