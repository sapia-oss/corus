package org.sapia.corus.core;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.core.ServiceNotFoundException;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.corus.client.services.naming.JndiModule;
import org.sapia.corus.util.IOUtils;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesTransformer;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


/**
 * An instance of this class acts as a Corus server's kernel.
 * It initializes the modules that are part of the server and
 * provides a method to lookup any given module.
 *
 * @author Yanick Duchesne
 */
public class CorusImpl implements Corus, RemoteContextProvider {
  
  private ModuleLifeCycleManager      lifeCycle;
  private String                      domain;

  CorusImpl(
      InputStream config, 
      String domain,
      TCPAddress serverAddress,
      CorusTransport aTransport, 
      String corusHome) throws IOException, Exception{
    init(config, domain, serverAddress, aTransport, corusHome);
  }

  public String getVersion() {
    return CorusVersion.create().toString();
  }
  
  public String getDomain() {
    return domain;
  }
  
  public RemoteContext getRemoteContext() throws RemoteException{
    JndiModule module = (JndiModule)lookup(JndiModule.ROLE);
    return module.getRemoteContext();
  }

  public ServerHost getHostInfo() {
    return lifeCycle.getHostInfo();
  }

  public ServerContext getServerContext(){
    return lifeCycle;
  }
  
  void setServerAddress(TCPAddress addr){
    lifeCycle.setServerAddress(addr);
  }
  
  @SuppressWarnings("deprecation")
  private ServerContext init(
                          InputStream config, 
                          String domain,
                          TCPAddress address,
                          CorusTransport aTransport, 
                          String corusHome) throws IOException, Exception {
    this.domain = domain;
    
    // loading default properties.
    final Properties props = new Properties();
    InputStream defaults = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/sapia/corus/default.properties");
    if(defaults == null){
      throw new IllegalStateException("Resource 'org/sapia/corus/default.properties' not found");
    }
    
    InputStream tmp = IOUtils.replaceVars(new PropertiesStrLookup(System.getProperties()), defaults);
    defaults.close();
    props.load(tmp);
    
    CompositeStrLookup lookup = new CompositeStrLookup();
    lookup.add(new PropertiesStrLookup(props)).add(new PropertiesStrLookup(System.getProperties()));
    tmp = IOUtils.replaceVars(lookup, config);
    props.load(tmp);
    
    // transforming Corus properties that correspond 1-to-1 to Ubik properties into their Ubik counterpart
    PropertiesUtil.transform(
    		props, 
    		PropertiesTransformer.MappedPropertiesTransformer.createInstance()
    			.add(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN, org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY)
    			.add(CorusConsts.PROPERTY_CORUS_MCAST_ADDRESS, 	org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY)
    			.add(CorusConsts.PROPERTY_CORUS_MCAST_PORT, 			org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY)
    );
    
    // copying Ubik-specific properties to the System properties. 
    PropertiesUtil.copy(
    		PropertiesUtil.filter(props, PropertiesFilter.NamePrefixPropertiesFilter.createInstance("ubik")), 
    		System.getProperties()
    );
    
    InternalServiceContext services = new InternalServiceContext();
    ServerContextImpl serverContext = new ServerContextImpl(this, aTransport, address, domain, corusHome, services, props);
    
    // root context
    PropertyContainer propContainer = new PropertyContainer() {
      @Override
      public String getProperty(String name) {
        return props.getProperty(name);
      }
    };
    
    final ModuleLifeCycleManager manager         = new ModuleLifeCycleManager(serverContext, propContainer);
    BeanFactoryPostProcessor configPostProcessor = new ConfigurationPostProcessor(manager);
    
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

    lifeCycle = manager;
 
    return serverContext;
  }
  
  public void start() throws Exception {
    lifeCycle.startServices();
  }
  
  public Object lookup(String module) throws ServiceNotFoundException{
    Object toReturn = lifeCycle.lookup(module);
    if(toReturn == null){
      throw new ServiceNotFoundException(String.format("No module found for: %s", module));
    }
    return toReturn;
  }
}
