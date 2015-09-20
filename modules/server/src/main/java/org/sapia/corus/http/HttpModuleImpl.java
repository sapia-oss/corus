package org.sapia.corus.http;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.http.filesystem.FileSystemExtension;
import org.sapia.corus.http.interop.SoapExtension;
import org.sapia.corus.http.jmx.JmxExtension;
import org.sapia.corus.http.ping.PingExtension;
import org.sapia.corus.http.rest.RestExtension;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link HttpModule} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = HttpModule.class)
public class HttpModuleImpl extends ModuleHelper implements HttpModule {

  private HttpExtensionManager httpExt, sslExt;
  
  @Autowired
  private SslExporter          sslExporter;

  private boolean apiSSLOnly;
  
  /**
   * Constructor for HttpModuleImpl.
   */
  public HttpModuleImpl() {
    super();
  }
  
  public void setApiSSLOnly(boolean apiSSLOnly) {
    this.apiSSLOnly = apiSSLOnly;
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
    httpExt = new HttpExtensionManager(logger(), serverContext());
    sslExt  = new HttpExtensionManager(logger(), serverContext());
    HttpTransportProvider transportProvider = (HttpTransportProvider) serverContext().getTransport().getTransportProvider();
    transportProvider.getRouter().setCatchAllHandler(httpExt);
  }

  @Override
  public void start() throws Exception {
    addHttpExtension(new PingExtension(serverContext));
    addHttpExtension(new FileSystemExtension(serverContext));
    addHttpExtension(new JmxExtension(serverContext));
    addHttpExtension(new SoapExtension(serverContext));
    if (apiSSLOnly) {
      logger().debug("Publishing REST extension over SSL only");
      addHttpsExtension(new RestExtension(serverContext));
    } else {
      addHttpExtension(new RestExtension(serverContext));
    }
    sslExporter.export(sslExt);
  }

  @Override
  public void dispose() {
  }

  @Override
  public void addHttpExtension(HttpExtension ext) {
    httpExt.addHttpExtension(ext);
    sslExt.addHttpExtension(ext);
  }

  @Override
  public void addHttpsExtension(HttpExtension ext) {
    sslExt.addHttpExtension(ext);
  }
}
