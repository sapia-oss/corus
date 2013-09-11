package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.UnknownHostException;

import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Strings;

/**
 * Holds misc information about a Corus host.
 * 
 * @author J-C Desrochers
 */
public class CorusHost implements Externalizable {
  
  /**
   * Indicates the role of the Corus node corresponding to this instance.
   */
  public enum RepoRole {
    
    /**
     * Indicates this node is neither a client or server
     * repository node.
     */
    NONE,
    
    /**
     * Indicates a repository client node.
     */
    CLIENT,
    
    /**
     * Indicates a repository server node.
     */
    SERVER;
  
    /**
     * @return <code>true</code> if this instance == {@link #CLIENT}.
     */
    public boolean isClient() {
      return this == CLIENT;
    }
    
    /**
     * @return <code>true</code> if this instance == {@link #SERVER}.
     */
    public boolean isServer() {
      return this == SERVER;
    }
  }
  
  static final long serialVersionUID = 1L;

  public static String localHostName;
  public static final String UNDEFINED_HOSTNAME = "N/A";
  
  private Endpoint      endpoint;
  private String 				osInfo;
  private String 				javaVmInfo;
  private RepoRole      role            = RepoRole.NONE;
  private String        hostName;
  
  static {
    try {
      localHostName = Localhost.getAnyLocalAddress().getHostName();
    } catch (UnknownHostException e) {
      localHostName = UNDEFINED_HOSTNAME;
    }
  }
  
  /**
   * Do not use directly: meant for externalization.
   */
  public CorusHost() {
  }
  
  /**
   * @param endpoint the {@link Endpoint} of the Corus node to which the new {@link CorusHost} will
   * correspond.
   * @param anOsInfo the OS info.
   * @param aJavaVmInfo the Java VM info.
   * @return a new {@link CorusHost}.
   */
  public static CorusHost newInstance(Endpoint endpoint, String anOsInfo, String aJavaVmInfo) {
    CorusHost created  = new CorusHost();
    created.endpoint   = endpoint;
    created.osInfo     = anOsInfo;
    created.javaVmInfo = aJavaVmInfo;
    created.hostName   = localHostName;
    return created;
  }
  
  /**
   * @return this instance's {@link Endpoint}.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * Returns the osInfo attribute.
   *
   * @return The osInfo value.
   */
  public String getOsInfo() {
    return osInfo;
  }

  /**
   * Changes the value of the attributes osInfo.
   *
   * @param aOsInfo The new value of the osInfo attribute.
   */
  public void setOsInfo(String aOsInfo) {
    osInfo = aOsInfo;
  }

  /**
   * Returns the javaVmInfo attribute.
   *
   * @return The javaVmInfo value.
   */
  public String getJavaVmInfo() {
    return javaVmInfo;
  }

  /**
   * Changes the value of the attributes javaVmInfo.
   *
   * @param aJavaVmInfo The new value of the javaVmInfo attribute.
   */
  public void setJavaVmInfo(String aJavaVmInfo) {
    javaVmInfo = aJavaVmInfo;
  }
  
  /**
   * @return this instance's {@link RepoRole}.
   */
  public RepoRole getRepoRole() {
    return role;
  }
  
  /**
   * Sets this instance's {@link RepoRole}.
   * 
   * @param role
   */
  public void setRepoRole(RepoRole role) {
    this.role = role;
  }
  
  
  /**
   * @param hostName a host name.
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }
  
  /**
   * @return the host name to which this instance corresponds.
   */
  public String getHostName() {
    return hostName;
  }
  
  /**
   * @return a pretty-print address corresponding to this instance, meant for display to users.
   */
  public String getFormattedAddress() {
    return hostName.equals(CorusHost.UNDEFINED_HOSTNAME) ?
           endpoint.getServerTcpAddress().toString() :
           hostName + ":" + endpoint.getServerTcpAddress().getPort();
  }
  
  // --------------------------------------------------------------------------
  // Externalization
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.endpoint      = (Endpoint) in.readObject();
    this.osInfo        = (String) in.readObject();
    this.javaVmInfo    = (String) in.readObject();
    this.role          = (RepoRole) in.readObject();
    this.hostName      = (String) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
    out.writeObject(osInfo);
    out.writeObject(javaVmInfo);
    out.writeObject(role);
    out.writeObject(hostName);
  }

  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return endpoint.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof CorusHost){
      CorusHost host = (CorusHost) obj;
      return endpoint.equals(host.getEndpoint());
    }
    return false;
   
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, 
        "endpoint", endpoint,
        "hostName", hostName,
        "os", osInfo, 
        "jvm", javaVmInfo, 
        "role", role);
  }
  
}
