package org.sapia.corus;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.log.Hierarchy;
import org.sapia.corus.admin.Corus;
import org.sapia.corus.admin.CorusVersion;
import org.sapia.corus.admin.services.naming.JndiModule;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.util.PropertyContainer;
import org.sapia.soto.SotoContainer;
import org.sapia.soto.util.Utils;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;
import org.sapia.util.text.MapContext;
import org.sapia.util.text.SystemContext;


/**
 * An instance of this class acts as a corus server's kernel.
 * It initializes the modules that are part of the server and
 * provides a method to lookup any given module.
 *
 * @author Yanick Duchesne
 */
public class CorusImpl implements Corus, RemoteContextProvider {
  private static SotoContainer  _cont;
  private static CorusImpl      _instance;
  private String                _domain;

  private CorusImpl(String domain) {
    _domain        = domain;
  }

  public String getVersion() {
    return CorusVersion.create().toString();
  }
  
  public String getDomain() {
    return _domain;
  }
  
  public static ServerContext init(Hierarchy h, InputStream config, String domain,
                          CorusTransport aTransport, String corusHome) throws java.io.IOException, Exception {
    _instance = new CorusImpl(domain);
    CorusRuntime.init(_instance, corusHome, aTransport);
    _cont = new SotoContainer();
    
    // loading default properties.
    final Properties props = new Properties();
    InputStream defaults = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/sapia/corus/default.properties");
    if(defaults == null){
      throw new IllegalStateException("Resource 'org/sapia/corus/default.properties' not found");
    }
    
    InputStream tmp = Utils.replaceVars(new SystemContext(), defaults, "org/sapia/corus/default.properties");
    defaults.close();
    props.load(tmp);
    
    // loading user properties (from config/corus.properties).
    //Properties userProps = new Properties();
    tmp = Utils.replaceVars(new MapContext(props, new SystemContext(), false), config, "config/corus.properties");
    config.close();
    props.load(tmp);
    
    InternalServiceContext services = new InternalServiceContext();
    ServerContext serverContext = new ServerContext(domain, corusHome,  services);
    InitContext.attach(new PropertyContainer(){
      public String getProperty(String name) {
        return props.getProperty(name);
      }
    },
    serverContext);
    try{
      _cont.load("org/sapia/corus/corus.conf", props);
    }finally{
      InitContext.unattach();
    }
    
    return serverContext;
  }
  
  public static void start() throws Exception {
    _cont.start();
  }
  
  public static void shutdown(){
    _cont.dispose();
  }
  
  public Object lookup(String module) throws CorusException {
    try {
      return _cont.lookup(module);
    } catch (Exception e) {
      e.printStackTrace();
      throw new CorusException(e);
    }
  }
  
  public static CorusImpl getInstance() {
    if (_instance == null) {
      throw new IllegalStateException("corus not initialized");
    }

    return _instance;
  }
  
  public RemoteContext getRemoteContext() throws RemoteException{
    try{
      JndiModule module = (JndiModule)lookup(JndiModule.ROLE);
      return module.getRemoteContext();
    }catch(CorusException e){
      throw new RuntimeException(e);
    }
  }
}
