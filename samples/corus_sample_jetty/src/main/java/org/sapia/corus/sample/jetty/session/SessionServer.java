package org.sapia.corus.sample.jetty.session;

import java.io.File;

public class SessionServer {

  public static void main(String[] args) throws Exception{
    
    File configFile = 
      new File(
          System.getProperty("user.dir")+
          File.separator+
          "etc/ehcache.xml"
      );
    SessionCache cache = new SessionCacheImpl(configFile);
    Remoting.bind(cache);
    
    System.out.println("Session server started");
    
    while(true){
      Thread.sleep(100000);
    }
  }
}
