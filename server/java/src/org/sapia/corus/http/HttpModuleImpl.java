package org.sapia.corus.http;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.http.filesystem.FileSystemExtension;
import org.sapia.corus.http.interop.SoapExtension;
import org.sapia.corus.http.jmx.JmxExtension;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpModuleImpl extends ModuleHelper implements HttpModule {

  private HttpExtensionManager _httpExt;
  
  /**
   * Constructor for HttpModuleImpl.
   */
  public HttpModuleImpl() {
    super();
  }
  
  /**
   * @see org.sapia.corus.admin.Module#getRoleName()
   */
  public String getRoleName() {
    return ROLE;
  }

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    // Create the interop and http extension transports
    String transportType = CorusRuntime.getTransport().getTransportProvider().getTransportType();
    if (transportType.equals(MultiplexSocketTransportProvider.TRANSPORT_TYPE)) {
      _httpExt = new HttpExtensionManager(logger());      
    } else if (transportType.equals(HttpTransportProvider.DEFAULT_HTTP_TRANSPORT_TYPE)) {
      _httpExt = new HttpExtensionManager(logger());      
    } else {
      throw new IllegalStateException("Could not initialize the http module using the transport type: " + transportType);
    }
    _httpExt.init();      
  }
  
  public void start() throws Exception {
    
    //////////// adding default extensions ///////////
    
    addHttpExtension(new FileSystemExtension());
    addHttpExtension(new JmxExtension());    
    SoapExtension ext = new SoapExtension(serverContext());
    addHttpExtension(ext);    
    
    _httpExt.start();
  }  
  
  public void dispose() {
    _httpExt.dispose();    
  }
 
  public void addHttpExtension(HttpExtension ext) {
    _httpExt.addHttpExtension(ext);
  }
}
