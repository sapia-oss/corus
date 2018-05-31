package org.sapia.corus.core;

import org.sapia.ubik.net.ServerAddress;

/**
 * Dispatched by the corus server once it has started listening to request.
 * 
 * @author Yanick Duchesne
 */
public class ServerStartedEvent implements java.io.Serializable {

  static final long serialVersionUID = 1L;

  private ServerAddress _address;

  public ServerStartedEvent(ServerAddress address) {
    _address = address;
  }

  /**
   * @return the <code>ServerAddress</code> that corresponds to the address of
   *         the corus server.
   */
  public ServerAddress getAddress() {
    return _address;
  }
}
