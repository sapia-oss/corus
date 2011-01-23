package org.sapia.corus.cluster;

import java.io.Serializable;

import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 */
public class CorusPubEvent implements Serializable {
  
  static final long serialVersionUID = 1L;
  
  private boolean       _new;
  private ServerAddress _origin;
  private ServerHost _hostInfo;

  public CorusPubEvent(boolean isNew, ServerAddress origin, ServerHost hostInfo) {
    _new    = isNew;
    _origin = origin;
    _hostInfo = hostInfo;
  }

  public boolean isNew() {
    return _new;
  }

  public ServerAddress getOrigin() {
    return _origin;
  }
  
  public ServerHost getHostInfo() {
    return _hostInfo;
  }
  
}
