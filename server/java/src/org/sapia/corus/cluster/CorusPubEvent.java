package org.sapia.corus.cluster;

import org.sapia.ubik.net.ServerAddress;

import java.io.Serializable;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusPubEvent implements Serializable {
  private boolean       _new;
  private ServerAddress _origin;

  /**
   * Constructor for CorusPubEvent.
   * @param arg0
   * @param arg1
   */
  public CorusPubEvent(boolean isNew, ServerAddress origin) {
    _new    = isNew;
    _origin = origin;
  }

  public boolean isNew() {
    return _new;
  }

  public ServerAddress getOrigin() {
    return _origin;
  }
}
