package org.sapia.corus.repository;

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
  
  @Override
  public boolean acceptsEvent(RepoEventType eventType) {
    return true;
  }

  @Override
  public boolean acceptsPull() {
    return true;
  }
}
