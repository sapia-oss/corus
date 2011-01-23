package org.sapia.corus.core;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.TransportProvider;


/**
 * This interface specifies the behavior of transport implementations for Corus. It is intended
 * to wrap Ubik's API, providing only the necessary methods. 
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
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
