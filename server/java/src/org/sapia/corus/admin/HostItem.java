package org.sapia.corus.admin;

import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HostItem {
  private ServerAddress _addr;
  private Object        _item;

  /**
   * Constructor for HostItem.
   */
  public HostItem(ServerAddress id, Object item) {
    _addr = id;
    _item = item;
  }

  public Object get() {
    return _item;
  }

  public ServerAddress getServerAddress() {
    return _addr;
  }
}
