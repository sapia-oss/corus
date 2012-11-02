package org.sapia.corus.core;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.corus.client.services.configurator.InternalConfigurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.net.TCPAddress;

/**
 * Implements the {@link ServerContext} interface.
 * 
 * @author yduchesne
 *
 */
public class ServerContextImpl implements ServerContext {

  static final String CORUS_PROCESS_FILE = "corus_process";
  
  private static final String _OS_INFO;
  private static final String _JAVA_VM_INFO;
  static {
    _OS_INFO = new StringBuilder().
            append(System.getProperty("os.name")).
            append(" ").
            append(System.getProperty("os.version")).
            toString();

    _JAVA_VM_INFO = new StringBuilder().
            append(System.getProperty("java.version")).
            append(" ").
            append(System.getProperty("java.vm.name")).
            toString();
  }

  private Corus 							   corus;
  private String 								 serverName = UUID.randomUUID().toString().substring(0, 8);
  private String 				 				 domain;
  private TCPAddress 		 				 serverAddress;
  private ServerHost 						 hostInfo;
  private CorusTransport 				 transport;
  private InternalServiceContext services;
  private String 								 homeDir;
  private Properties             properties;
  
  public ServerContextImpl(
      Corus corus,
      CorusTransport transport,
      TCPAddress addr, 
      String domain, 
      String homeDir, 
      InternalServiceContext services,
      Properties props){
    this.corus 				 = corus;
    this.transport     = transport;
    this.serverAddress = addr;
    this.domain        = domain;
    this.homeDir       = homeDir;
    this.services      = services;
    this.hostInfo      = ServerHost.createNew(addr, _OS_INFO, _JAVA_VM_INFO);
    this.properties    = props;
  }
  
  @Override
  public Corus getCorus() {
    return corus;
  }
  
  @Override
  public String getServerName() {
    return serverName;
  }
  
  void setServerName(String serverName) {
    this.serverName = serverName;
  }

  @Override
  public void overrideServerName(String serverName) {
    this.serverName = serverName;
  }
  
  @Override
  public String getHomeDir() {
    return homeDir;
  }
  
  @Override
  public String getDomain() {
    return domain;
  }
  
  @Override  
  public TCPAddress getServerAddress() {
    return serverAddress;
  }
  
  @Override
  public ServerHost getHostInfo() {
    return hostInfo;
  }
  
  @Override
  public CorusTransport getTransport() {
    return transport;
  }
  
  @Override
  public InternalServiceContext getServices() {
    return services;
  }
  
  @Override
  public <S> S lookup(Class<S> serviceInterface){
    return services.lookup(serviceInterface);
  }
  
  @Override
  public Object lookup(String name) {
    return services.lookup(name);
  }
  
  @Override
  public Properties getCorusProperties() {
	  return properties;
  }
  
  @Override
  public Properties getProcessProperties() throws IOException{
    Properties processProps   = new Properties();
    
    // ------------------------------------------------------------------------
    // copying Ubik properties to process properties
    Properties ubikProperties = PropertiesUtil.filter(
    		System.getProperties(), 
    		PropertiesFilter.NamePrefixPropertiesFilter.createInstance("ubik")
    );
    PropertiesUtil.copy(ubikProperties, processProps);

    // ------------------------------------------------------------------------
    // loading process properties from file
    // (trying "global" file and then domain-specific file
    File home = new File(getHomeDir() + File.separator + "config");
    PropertiesUtil.loadIfExist(processProps, new File(home, CORUS_PROCESS_FILE + ".properties"));
    PropertiesUtil.loadIfExist(processProps, new File(home, CORUS_PROCESS_FILE + "_" + getDomain() + ".properties"));
    
    // ------------------------------------------------------------------------
    // copying configurator props to process props
    Properties configuratorProps = services.lookup(InternalConfigurator.class).getInternalProperties(PropertyScope.PROCESS);
    PropertiesUtil.copy(configuratorProps, processProps);
    return processProps;
  }
  
  void setServerAddress(TCPAddress addr){
    this.serverAddress = addr;
  }

}
