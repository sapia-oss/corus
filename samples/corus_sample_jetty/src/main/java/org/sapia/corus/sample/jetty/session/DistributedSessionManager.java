package org.sapia.corus.sample.jetty.session;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.session.AbstractSessionManager;

public class DistributedSessionManager extends AbstractSessionManager{
  
  private SessionCache sessions;
  
  public DistributedSessionManager(SessionCache sessions){ 
    this.sessions = sessions;
  }
  
  @Override
  protected void addSession(Session aSession) {    
    sessions.put(aSession);
  }
  
  @Override
  public Session getSession(String id) {
    return sessions.get(id);
  }
  
  @Override
  protected Session newSession(HttpServletRequest request) {
    return new DistributedSession(request);
  }
  
  @Override
  public Map getSessionMap() {
    return new HashMap();
  }
  
  @Override
  protected void removeSession(String sessionId) {
    sessions.remove(sessionId);
  }
  
  @Override
  protected void invalidateSessions() {
  }
  
  /////////////////////////////////////////////////////////////////////
  
  class DistributedSession extends AbstractSessionManager.Session{
    
    DistributedSession(HttpServletRequest request){
      super(request);
    }
    
    DistributedSession(long created, String clusterId){
      super(created, clusterId);
    }
    
    @Override
    protected Map newAttributeMap() {
      return new HashMap();
    }
    
    
  }


}
