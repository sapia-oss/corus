package org.sapia.corus.admin;

import org.sapia.ubik.net.ServerAddress;


/**
 * Wraps a single object returned by a Corus server.
 * @author Yanick Duchesne
 */
public class HostItem {
  private ServerAddress _addr;
  private Object        _item;

  /**
   * @param addr the address of the server
   * @param item the {@link Object} that is returned by
   * the server.
   */
  public HostItem(ServerAddress addr, Object item) {
    _addr = addr;
    _item = item;
  } 

  /**
   * @return the {@link Object} that was returned by the server.
   */
  public Object get() {
    return _item;
  }

  /**
   * @return the {@link ServerAddress} of the server that returned
   * the encapsulated object.
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }
}
