package org.sapia.corus;

import java.rmi.RemoteException;
import org.apache.log.Hierarchy;
import org.sapia.corus.naming.JndiModule;

import org.sapia.soto.SotoContainer;
import org.sapia.soto.util.Utils;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;

import org.sapia.util.text.MapContext;
import org.sapia.util.text.SystemContext;

import java.io.InputStream;
import java.util.Properties;


/**
 * An instance of this class acts as a corus server's kernel.
 * It initializes the modules that are part of the server and
 * provides a method to lookup any given module.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusImpl implements Corus, RemoteContextProvider {
  private static SotoContainer  _cont;
  private static CorusImpl _instance;
  private String                _domain;

  private CorusImpl(String domain) {
    _domain        = domain;
    }

  public String getDomain() {
    return _domain;
  }
  
  public static void init(Hierarchy h, InputStream config, String domain,
                          CorusTransport aTransport, String corusHome) throws java.io.IOException, Exception {
    _instance = new CorusImpl(domain);
    CorusRuntime.init(_instance, corusHome, aTransport);
    _cont = new SotoContainer();
    
    // loading default properties.
    Properties props = new Properties();
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
    _cont.load("org/sapia/corus/corus.conf", props);
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
