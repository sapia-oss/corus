package org.sapia.corus.core;

import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Properties;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.exceptions.core.ServiceNotFoundException;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.naming.JndiModule;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * An instance of this class acts as a Corus server's kernel. It initializes the
 * modules that are part of the server and provides a method to lookup any given
 * module.
 * 
 * @author Yanick Duchesne
 */
@Remote(interfaces = { Corus.class, RemoteContextProvider.class})
public class CorusImpl implements InternalCorus, RemoteContextProvider {

  private ModuleLifeCycleManager lifeCycle;
  private volatile String domain;
  private KeyPair keyPair;

  CorusImpl(Properties config, String domain, ServerAddress serverAddress, EventChannel channel, CorusTransport aTransport, String corusHome, KeyPair keyPair)
      throws IOException, Exception {
    init(config, domain, serverAddress, channel, aTransport, corusHome, keyPair);
  }

  @Override
  public String getVersion() {
    return CorusVersion.create().toString();
  }

  @Override
  public String getDomain() {
    return domain;
  }
  
  @Override
  public PublicKey getPublicKey() {
    return keyPair.getPublic();
  }
  
  @Override
  public void changeDomain(String newDomainName) {
    Properties props = new Properties();
    props.setProperty(CorusConsts.PROPERTY_CORUS_DOMAIN, newDomainName);
    CorusReadonlyProperties.save(
        props, 
        CorusConsts.CORUS_USER_HOME, 
        lifeCycle.getCorusHost().getEndpoint().getServerTcpAddress().getPort(), 
        false
    );
    this.domain = newDomainName;
    System.setProperty(CorusConsts.PROPERTY_CORUS_DOMAIN, domain);
  }

  public RemoteContext getRemoteContext() throws RemoteException {
    JndiModule module = (JndiModule) lookup(JndiModule.ROLE);
    return module.getRemoteContext();
  }

  public CorusHost getHostInfo() {
    return lifeCycle.getCorusHost();
  }

  public ServerContext getServerContext() {
    return lifeCycle;
  }

  private ServerContext init(final Properties props, String domain, ServerAddress address, EventChannel channel, CorusTransport aTransport,
      String corusHome, KeyPair keyPair) throws IOException, Exception {
    this.domain = domain;
    this.keyPair = keyPair;
    InternalServiceContext services = new InternalServiceContext();
    String fixedCorusHome = FileUtil.fixFileSeparators(corusHome);
    ServerContextImpl serverContext = new ServerContextImpl(this, aTransport, address, channel, domain, fixedCorusHome, services, props, keyPair);

    // root context
    PropertyContainer propContainer = new PropertyContainer() {
      @Override
      public String getProperty(String name) {
        return props.getProperty(name);
      }
    };

    final ModuleLifeCycleManager manager = new ModuleLifeCycleManager(serverContext, propContainer);
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

  public Object lookup(String module) throws ServiceNotFoundException {
    Object toReturn = lifeCycle.lookup(module);
    if (toReturn == null) {
      throw new ServiceNotFoundException(String.format("No module found for: %s", module));
    }
    return toReturn;
  }
  
  @Override
  public String dump() {
    StringWriter writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    lifeCycle.dump(stream);
    return writer.toString();
  }
}
