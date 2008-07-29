package org.sapia.corus;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched by the corus server once it has started listening to request.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerStartedEvent implements java.io.Serializable, Event{
	
	private ServerAddress _address;

	public ServerStartedEvent(ServerAddress address){
		_address  = address; 
	}
	
	/**
	 * @return the <code>ServerAddress</code> that corresponds to the address of the 
	 * corus server.
	 */
	public ServerAddress getAddress(){
		return _address;
	}
}
