package org.sapia.corus;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.TransportProvider;


/**
 * This interface specifies the behavior of transport implementations for Corus. It is intended
 * to wrap Ubik's API, providing only the necessary methods. 
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface CorusTransport {

  /**
   * @return The server address used by this Corus transport.
   */
  public ServerAddress getServerAddress();


  /**
   * Remotely exports the object passed in using the current transport mechanism.
   * 
   * @param anObject The object to export.
   * @return the stub that was generated for the given object.
   * @throws RemoteException If an error occurs exporting the object.
   */
  public Object exportObject(Object anObject) throws RemoteException;


  /**
   * Shuts down the underlying transport implementation.
   */
  public void shutdown();
  
  
  /**
   * @return The Ubik transport provider used by this corus transport implementation.
   */
  public TransportProvider getTransportProvider();
}
