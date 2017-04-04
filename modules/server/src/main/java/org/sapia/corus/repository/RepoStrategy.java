package org.sapia.corus.repository;

/**
 * A strategy that abstracts how repo servers/clients react to incoming events. This strategy
 * is meant to allow repo servers to accept being coordinated by their peers when relevant
 * (that is, when they have nothing on them as they appear on the network).
 * 
 * @see RepoServerSyncStrategy
 * 
 * @author yduchesne
 *
 */
public interface RepoStrategy {

  /**
   * @return <code>true</code> if this instance accepts the given event type.
   */
  public boolean acceptsEvent(RepoEventType eventType);
  
  /**
   * @return <code>true</code> if this instance accepts that the repo node pulls data
   * from repo servers.
   */
  public boolean acceptsPull();
  
}
