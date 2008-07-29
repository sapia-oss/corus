package org.sapia.corus.admin;

import java.util.ArrayList;
import java.util.Collection;

import org.sapia.ubik.net.ServerAddress;


/**
 * Encapsulates objects from a given host.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HostList extends ArrayList {
  private ServerAddress _addr;

  /**
   * Constructor for HostList.
   * @param arg0
   */
  public HostList(ServerAddress addr, int capacity) {
    super(capacity);
    _addr = addr;
  }

  /**
   * Constructor for HostList.
   * @param arg0
   */
  public HostList(ServerAddress addr, Collection arg0) {
    super(arg0);
    _addr = addr;
  }

  /**
   * Constructor for HostList.
   */
  public HostList(ServerAddress addr) {
    super();
    _addr = addr;
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
