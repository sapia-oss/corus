package org.sapia.corus.core;

import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;

/**
 * Implements the {@link ServerContext} interface.
 * 
 * @author yduchesne
 * 
 */
public class ServerContextImpl implements ServerContext {

  static final String CORUS_PROCESS_FILE = "corus_process";

  private static final String OS_INFO;
  private static final String JAVA_VM_INFO;
  static {
    OS_INFO = new StringBuilder().append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).toString();

    JAVA_VM_INFO = new StringBuilder().append(System.getProperty("java.version")).append(" ").append(System.getProperty("java.vm.name")).toString();
  }

  private Corus corus;
  private String serverName = UUID.randomUUID().toString().substring(0, 8);
  private String domain;
  private CorusHost hostInfo;
  private CorusTransport transport;
  private EventChannel eventChannel;
  private InternalServiceContext services;
  private String homeDir;
  private Properties properties;
  private KeyPair keyPair;

  public ServerContextImpl(Corus corus, CorusTransport transport, ServerAddress serverAddress, EventChannel channel, String domain, String homeDir,
      InternalServiceContext services, Properties props, KeyPair keyPair) {
    this.corus = corus;
    this.transport = transport;
    this.eventChannel = channel;
    this.hostInfo = CorusHost.newInstance(channel.getNode(), new Endpoint(serverAddress, channel.getUnicastAddress()), OS_INFO, JAVA_VM_INFO, keyPair.getPublic());
    this.domain = domain;
    this.homeDir = homeDir;
    this.services = services;
    this.properties = props;
    this.keyPair = keyPair;
    String repoRoleProp = properties.getProperty(CorusConsts.PROPERTY_REPO_TYPE, CorusHost.RepoRole.NONE.name());
    hostInfo.setRepoRole(RepoRole.valueOf(repoRoleProp.toUpperCase()));
  }

  @Override
  public Corus getCorus() {
    return corus;
  }

  @Override
  public KeyPair getKeyPair() {
    return keyPair;
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
  public String getNodeSubdirName() {
    return "port_" + getCorusHost().getEndpoint().getServerTcpAddress().getPort();
  }

  @Override
  public String getDomain() {
    return domain;
  }

  @Override
  public CorusHost getCorusHost() {
    return hostInfo;
  }

  @Override
  public CorusTransport getTransport() {
    return transport;
  }

  @Override
  public EventChannel getEventChannel() {
    return eventChannel;
  }

  @Override
  public InternalServiceContext getServices() {
    return services;
  }

  @Override
  public <S> S lookup(Class<S> serviceInterface) {
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
  public Properties getProcessProperties(List<String> categories) throws IOException {
    return services.lookup(Configurator.class).getProperties(PropertyScope.PROCESS, categories);
  }

  void setHostInfo(CorusHost hostInfo) {
    this.hostInfo = hostInfo;
  }

}
