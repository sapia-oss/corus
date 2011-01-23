package org.sapia.corus.http;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.http.filesystem.FileSystemExtension;
import org.sapia.corus.http.interop.SoapExtension;
import org.sapia.corus.http.jmx.JmxExtension;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;


/**
 * Implements the {@link HttpModule} interface.
 * @author Yanick Duchesne
 */
@Bind(moduleInterface=HttpModule.class)
public class HttpModuleImpl extends ModuleHelper implements HttpModule {

  private HttpExtensionManager _httpExt;
  
  /**
   * Constructor for HttpModuleImpl.
   */
  public HttpModuleImpl() {
    super();
  }
  
  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return ROLE;
  }

  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    // Create the interop and http extension transports
    String transportType = serverContext().getTransport().getTransportProvider().getTransportType();
    if (transportType.equals(MultiplexSocketTransportProvider.TRANSPORT_TYPE)) {
      _httpExt = new HttpExtensionManager(logger(), serverContext());      
    } else if (transportType.equals(HttpTransportProvider.DEFAULT_HTTP_TRANSPORT_TYPE)) {
      _httpExt = new HttpExtensionManager(logger(), serverContext());      
    } else {
      throw new IllegalStateException("Could not initialize the http module using the transport type: " + transportType);
    }
    _httpExt.init();      
  }
  
  /**
   * @see Service#start()
   */
  public void start() throws Exception {
    
    //////////// adding default extensions ///////////
    
    addHttpExtension(new FileSystemExtension(serverContext()));
    addHttpExtension(new JmxExtension(serverContext()));    
    SoapExtension ext = new SoapExtension(serverContext());
    addHttpExtension(ext);    
    
    _httpExt.start();
  }  
  
  /**
   * @see Service#dispose()
   */
  public void dispose() {
    _httpExt.dispose();    
  }
 
  /**
   * Adds a {@link HttpExtension} to this instance.
   * 
   * @param ext a {@link HttpExtension}
   */
  public void addHttpExtension(HttpExtension ext) {
    _httpExt.addHttpExtension(ext);
  }
}
