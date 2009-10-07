package org.sapia.corus.admin;

import java.util.ArrayList;
import java.util.Collection;

import org.sapia.ubik.net.ServerAddress;


/**
 * Encapsulates objects from a given host.
 *
 * @author Yanick Duchesne
 */
public class HostList<T> extends ArrayList<T> {
  
  static final long serialVersionUID = 1L;
  
  private ServerAddress _addr;

  /**
   * @param addr the {@link ServerAddress} of the Corus server.
   * @param capacity the capacity (and growth increment) if this instance's
   * internal storage structure.
   */
  public HostList(ServerAddress addr, int capacity) {
    super(capacity);
    _addr = addr;
  }

  /**
   * Allows passing in a {@link Collection} of objects that
   * will populate this instance.
   * 
   * @see #HostList(ServerAddress, int)
   */
  public HostList(ServerAddress addr, Collection<T> objects) {
    super(objects);
    _addr = addr;
  }

  /**
   * @see #HostList(ServerAddress, int)
   */
  public HostList(ServerAddress addr) {
    this(addr, 10);
  }

  /**
   * Returns this instance's server address.
   *
   * @return this instance's <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress() {
    return _addr;
  }
}
