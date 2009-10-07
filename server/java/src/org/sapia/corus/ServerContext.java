package org.sapia.corus;

import java.util.UUID;

import org.sapia.ubik.net.TCPAddress;

/**
 * Encapsulates the state pertaining to a corus server.
 * 
 * @author yduchesne
 *
 */
public class ServerContext {

  private String serverName = UUID.randomUUID().toString().substring(0, 8);
  private String domain;
  private TCPAddress serverAddress;
  private InternalServiceContext services;
  private String homeDir;
  
  public ServerContext(
      TCPAddress addr, 
      String domain, 
      String homeDir, 
      InternalServiceContext services){
    this(domain, homeDir, services);
    this.serverAddress = addr;
  }
  
  public ServerContext(
      String domain, 
      String homeDir, 
      InternalServiceContext services){
    this.domain = domain;
    this.homeDir = homeDir;
    this.services = services;
  }
  
  /**
   * @return the name of the Corus server.
   */
  public String getServerName() {
    return serverName;
  }
  
  /**
   * @param serverName the name of the Corus server.
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }
  
  /**
   * @return the home directory of the Corus server.
   */
  public String getHomeDir() {
    return homeDir;
  }
  
  /**
   * @return the domain of the Corus server.
   */
  public String getDomain() {
    return domain;
  }
  
  /**
   * @return the address of the Corus server corresponding to this
   * instance.
   */
  public TCPAddress getServerAddress() {
    if(serverAddress == null){
      throw new IllegalStateException("Server address not yet available");
    }
    return serverAddress;
  }
  
  /**
   * @return the {@link InternalServiceContext} containing the services
   * of the Corus server.
   */
  public InternalServiceContext getServices() {
    return services;
  }
  
  /**
   * Looks up a service of the given interface (internally delegates the call to
   * this instances {@link InternalServiceContext}.
   * @param <S> a service interface type
   * @param serviceInterface the service interface for which to find a service instance.
   * @return the service instance that was found.
   */
  public <S> S lookup(Class<S> serviceInterface){
    return services.lookup(serviceInterface);
  }
  
  void setServerAddress(TCPAddress addr){
    this.serverAddress = addr;
  }

}
