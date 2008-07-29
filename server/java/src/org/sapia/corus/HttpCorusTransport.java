package org.sapia.corus;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;


/**
 * HTTP implementation of the <code>CorusTransport</code> interface.
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
public class HttpCorusTransport extends AbstractTransport {

  public static final String HTTP_CONTEXT = "/ubik";

  /** The transport provider instance. */
  private HttpTransportProvider _theTransportProvider;

  /** The server address. */
  private HttpAddress _theAddress;
  
  /**
   * Creates a new HttpCorusTransport instance.
   * 
   * @param aPort The number of port on which to export objects.
   */
  public HttpCorusTransport(String aHost, int aPort) {
    if (aHost == null || aHost.length()== 0) {
      throw new IllegalStateException("The host passed in is invalid: " + aHost);
    } else if (aPort <= 0) {
      throw new IllegalStateException("The port number is invalid: " + aPort);
    }
    
    Uri anUri = new Uri("http", aHost, aPort, HTTP_CONTEXT);
    _theAddress = new HttpAddress(anUri);

    _theTransportProvider = new HttpTransportProvider();
    TransportManager.registerProvider(_theTransportProvider);

  }
  
  /**
   * Returns the server address used by this Corus transport.
   * 
   * @return The server address used by this Corus transport.
   */
  public ServerAddress getServerAddress() {
    return _theAddress;
  }
  
  /**
   * @see org.sapia.corus.AbstractTransport#initExport(java.lang.Object)
   */
  protected Object initExport(Object anObject) throws RemoteException {
    Properties someProps = new Properties();
    someProps.setProperty(Consts.TRANSPORT_TYPE,
                          _theTransportProvider.getTransportType());
    someProps.setProperty(HttpTransportProvider.HTTP_PORT_KEY,
                          String.valueOf(_theAddress.getPort()));

    return Hub.exportObject(anObject, someProps);
  }
  
  /**
   * Returns the Ubik transport provider used by this corus transport implementation.
   * 
   * @return The Ubik transport provider used by this corus transport implementation.
   */
  public TransportProvider getTransportProvider() {
    return _theTransportProvider;
  }
}
