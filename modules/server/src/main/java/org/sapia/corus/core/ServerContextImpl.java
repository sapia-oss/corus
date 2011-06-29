package org.sapia.corus.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.ubik.net.TCPAddress;

/**
 * Encapsulates the state pertaining to a corus server.
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

  private Corus corus;
  private String serverName = UUID.randomUUID().toString().substring(0, 8);
  private String domain;
  private TCPAddress serverAddress;
  private ServerHost hostInfo;
  private CorusTransport transport;
  private InternalServiceContext services;
  private String homeDir;
  
  public ServerContextImpl(
      Corus corus,
      CorusTransport transport,
      TCPAddress addr, 
      String domain, 
      String homeDir, 
      InternalServiceContext services){
    this.corus = corus;
    this.transport = transport;
    this.serverAddress = addr;
    this.domain = domain;
    this.homeDir = homeDir;
    this.services = services;
    this.hostInfo = ServerHost.createNew(addr, _OS_INFO, _JAVA_VM_INFO);
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
  
  /* (non-Javadoc)
   * @see org.sapia.corus.core.ServerContext#getHostInfo()
   */
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
  public Properties getProcessProperties() throws IOException{
    File home = new File(getHomeDir() + File.separator + "config");
    File globalProps = new File(home, CORUS_PROCESS_FILE + ".properties");
    Properties globals = new Properties();
    if(globalProps.exists()){
      FileInputStream stream = new FileInputStream(globalProps);
      try{
        globals.load(stream);
      }finally{
        stream.close();
      }
    }
    File domainProps = new File(home, CORUS_PROCESS_FILE + "_" + getDomain() + ".properties");
    if(domainProps.exists()){
      FileInputStream stream = new FileInputStream(domainProps);
      try{
        globals.load(stream);
      }finally{
        stream.close();
      }
    }    
    Properties configuratorProps = services.getConfigurator().getProperties(PropertyScope.PROCESS);
    Enumeration propNames = configuratorProps.propertyNames();
    while(propNames.hasMoreElements()){
      String name  = (String)propNames.nextElement();
      String value = configuratorProps.getProperty(name);
      globals.setProperty(name, value);
    }
    return globals;
  }
  
  void setServerAddress(TCPAddress addr){
    this.serverAddress = addr;
  }

}
