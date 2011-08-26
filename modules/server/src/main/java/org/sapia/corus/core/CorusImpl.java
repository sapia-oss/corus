package org.sapia.corus.core;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.log.Hierarchy;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.core.ServiceNotFoundException;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.corus.client.services.naming.JndiModule;
import org.sapia.corus.util.IOUtils;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


/**
 * An instance of this class acts as a corus server's kernel.
 * It initializes the modules that are part of the server and
 * provides a method to lookup any given module.
 *
 * @author Yanick Duchesne
 */
public class CorusImpl implements Corus, RemoteContextProvider {
  
  private ModuleLifeCycleManager      _lifeCycle;
  private String                      _domain;

  CorusImpl(
      Hierarchy h, 
      InputStream config, 
      String domain,
      TCPAddress serverAddress,
      CorusTransport aTransport, 
      String corusHome) throws IOException, Exception{
    init(h, config, domain, serverAddress, aTransport, corusHome);
  }

  public String getVersion() {
    return CorusVersion.create().toString();
  }
  
  public String getDomain() {
    return _domain;
  }
  
  public RemoteContext getRemoteContext() throws RemoteException{
    JndiModule module = (JndiModule)lookup(JndiModule.ROLE);
    return module.getRemoteContext();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.Corus#getHostInfo()
   */
  public ServerHost getHostInfo() {
    return _lifeCycle.getHostInfo();
  }

  public ServerContext getServerContext(){
    return _lifeCycle;
  }
  
  void setServerAddress(TCPAddress addr){
    _lifeCycle.setServerAddress(addr);
  }
  
  private ServerContext init(
                          Hierarchy h, 
                          InputStream config, 
                          String domain,
                          TCPAddress address,
                          CorusTransport aTransport, 
                          String corusHome) throws IOException, Exception {
    _domain = domain;
    
    // loading default properties.
    final Properties props = new Properties();
    InputStream defaults = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/sapia/corus/default.properties");
    if(defaults == null){
      throw new IllegalStateException("Resource 'org/sapia/corus/default.properties' not found");
    }
    
    InputStream tmp = IOUtils.replaceVars(new PropertiesStrLookup(System.getProperties()), defaults);
    defaults.close();
    props.load(tmp);
    
    // loading user properties (from config/corus.properties).
    //Properties userProps = new Properties();
    CompositeStrLookup lookup = new CompositeStrLookup();
    lookup.add(new PropertiesStrLookup(props)).add(new PropertiesStrLookup(System.getProperties()));
    tmp = IOUtils.replaceVars(lookup, config);
    props.load(tmp);
    
    InternalServiceContext services = new InternalServiceContext();
    ServerContextImpl serverContext = new ServerContextImpl(this, aTransport, address, domain, corusHome, services);
    
    // root context
    PropertyContainer propContainer = new PropertyContainer() {
      @Override
      public String getProperty(String name) {
        return props.getProperty(name);
      }
    };
    final ModuleLifeCycleManager manager                   = new ModuleLifeCycleManager(serverContext, propContainer);
    BeanFactoryPostProcessor configPostProcessor           = new ConfigurationPostProcessor(serverContext, manager);

    GenericApplicationContext rootContext = new GenericApplicationContext();
    rootContext.getBeanFactory().registerSingleton("lifecycleManager", manager);
    rootContext.refresh();
    
    // core services context
    ClassPathXmlApplicationContext coreContext = new ClassPathXmlApplicationContext(rootContext);
    coreContext.addBeanFactoryPostProcessor(configPostProcessor);
    coreContext.registerShutdownHook();
    coreContext.setConfigLocation("org/sapia/corus/core.xml");
    coreContext.refresh();
    manager.addApplicationContext(coreContext);
    
    // module context
    ClassPathXmlApplicationContext moduleContext = new ClassPathXmlApplicationContext(coreContext);
    moduleContext.addBeanFactoryPostProcessor(configPostProcessor);
    moduleContext.registerShutdownHook();
    moduleContext.setConfigLocation("org/sapia/corus/modules.xml");
    moduleContext.refresh();
    manager.addApplicationContext(moduleContext);

    _lifeCycle = manager;
 
    return serverContext;
  }
  
  public void start() throws Exception {
    _lifeCycle.startServices();
  }
  
  public Object lookup(String module) throws ServiceNotFoundException{
    Object toReturn = _lifeCycle.lookup(module);
    if(toReturn == null){
      throw new ServiceNotFoundException(String.format("No module found for: %s", module));
    }
    return toReturn;
  }
}
