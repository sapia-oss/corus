package org.sapia.corus.sample.jetty.session;

import java.rmi.Remote;

import org.eclipse.jetty.server.session.AbstractSessionManager.Session;

public interface SessionCache extends Remote{
  
  public void put(Session session);
  
  public Session get(String sessionId);
 
  public void remove(String sessionId);
}
