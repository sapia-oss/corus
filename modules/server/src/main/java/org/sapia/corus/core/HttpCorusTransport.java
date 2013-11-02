package org.sapia.corus.core;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.sapia.ubik.util.Localhost;


/**
 * Implements the {@link CorusTransport} interface over HTTP.
 * 
 * @author yduchesne
 *
 */
public class HttpCorusTransport extends AbstractTransport {
  
  /** The server address. */
  private HttpAddress              address;
  
  /**
   * Creates a new TcpCorusTransport instance.
   * 
   * @param port the server port.
   */
  public HttpCorusTransport(int port) throws UnknownHostException {
    if (port <= 0) {
      throw new IllegalStateException("The port number is invalid: " + port);
    }
    address = HttpAddress.newDefaultInstance(Localhost.getAnyLocalAddress().getHostAddress(), port);
  }

  /**
   * @return this instance's {@link ServerAddress}.
   */
  public ServerAddress getServerAddress() {
    return address;
  }
  
  /**
   * @see org.sapia.corus.core.AbstractTransport#initExport(java.lang.Object)
   */
  protected Object initExport(Object anObject) throws RemoteException {
    Properties props = new Properties();
    props.setProperty(HttpTransportProvider.HTTP_PORT_KEY, Integer.toString(address.getPort()));
    props.setProperty(Consts.TRANSPORT_TYPE, HttpTransportProvider.HTTP_TRANSPORT_TYPE);
    return Hub.exportObject(anObject, props);
  }

  /**
   * @return The Ubik transport provider used by this corus transport implementation.
   */
  public TransportProvider getTransportProvider() {
    return Hub.getModules().getTransportManager().getProviderFor(HttpTransportProvider.HTTP_TRANSPORT_TYPE);
  }
}
