package org.sapia.corus.event;

import org.sapia.soto.Service;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;

/**
 * Implements the <code>EventDispatcher</code> interface.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class EventDispatcherImpl extends MultiDispatcher implements EventDispatcher, Service{
	
  /**
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return ROLE;
  }
  
  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {}
  
  /**
   * @see org.sapia.soto.Service#start()
   */
  public void start() throws Exception {}
  
  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {}

}
