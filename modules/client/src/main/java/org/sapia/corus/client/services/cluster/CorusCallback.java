package org.sapia.corus.client.services.cluster;

import java.util.Set;

import org.sapia.ubik.net.ServerAddress;

public interface CorusCallback {

  public ServerAddress getCorusAddress();
  
  public Set<ServerAddress> getSiblings();
  
  public Object lookup(String moduleName);
  
  public void debug(String message);
  
  public boolean isDebug();
  
  public void error(String message, Throwable err);
  
  public Object send(ClusteredCommand cmd, ServerAddress nextTarget) throws Throwable ;
}
