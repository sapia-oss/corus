package org.sapia.corus.client.services.cluster;

import java.util.Set;

import org.sapia.corus.client.Corus;
import org.sapia.ubik.net.ServerAddress;

public interface CorusCallback {

  public Set<ServerAddress> getSiblings();
  
  public Corus getCorus();
  
  public void debug(String message);
  
  public boolean isDebug();
  
  public void error(String message, Throwable err);
  
  public Object send(ClusteredCommand cmd, ServerAddress nextTarget) throws Throwable ;
}
