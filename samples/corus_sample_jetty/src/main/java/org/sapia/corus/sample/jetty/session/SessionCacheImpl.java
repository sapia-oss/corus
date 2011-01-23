package org.sapia.corus.sample.jetty.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.eclipse.jetty.server.session.AbstractSessionManager.Session;

public class SessionCacheImpl implements SessionCache{
  
  private CacheManager caches;
  private Cache sessions;
  
  public SessionCacheImpl(File configFile) throws IOException{
    InputStream config = new FileInputStream(configFile);
    try{
      caches = CacheManager.create(config);
      sessions = caches.getCache("sessions");
    }finally{
      config.close();
    }
  }
  
  @Override
  public Session get(String sessionId) {
    Element sessionElement = sessions.get(sessionId);
    Session session = (Session)sessionElement.getObjectValue();
    return session;
  }
  
  
  @Override
  public void put(Session session) {
    Element sessionElement = new Element(session.getId(), session);
    sessionElement.setTimeToIdle(session.getMaxInactiveInterval());
    sessions.put(sessionElement);
  }
  
  @Override
  public void remove(String sessionId) {
    sessions.remove(sessionId);
  }
}
