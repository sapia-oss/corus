package org.sapia.corus.core;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketAddress;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;


/**
 * Raw-socket implementation of the <code>CorusTransport</code> interface.
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class TcpCorusTransport extends AbstractTransport{

  /** The server address. */
  private TCPAddress _theAddress;

  /**
   * Creates a new TcpCorusTransport instance.
   * 
   * @param aPort The number of port on which to export objects.
   */
  public TcpCorusTransport(String aHost, int aPort) {
    if (aHost == null || aHost.length()== 0) {
      throw new IllegalStateException("The host passed in is invalid: " + aHost);
    } else if (aPort <= 0) {
      throw new IllegalStateException("The port number is invalid: " + aPort);
    }
    
    _theAddress = new MultiplexSocketAddress(aHost, aPort);
  }

  /**
   * @return The server address used by this Corus transport.
   */
  public ServerAddress getServerAddress() {
    return _theAddress;
  }
  
  /**
   * @see org.sapia.corus.core.AbstractTransport#initExport(java.lang.Object)
   */
  protected Object initExport(Object anObject) throws RemoteException {
    Properties props = new Properties();
    props.setProperty(MultiplexSocketTransportProvider.BIND_ADDRESS, _theAddress.getHost());
    props.setProperty(MultiplexSocketTransportProvider.PORT, ""+_theAddress.getPort());
    props.setProperty(Consts.TRANSPORT_TYPE, MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE);
    return Hub.exportObject(anObject, props);
  }

  /**
   * @return The Ubik transport provider used by this corus transport implementation.
   */
  public TransportProvider getTransportProvider() {
    return Hub.getModules().getTransportManager().getDefaultProvider();
  }
}
