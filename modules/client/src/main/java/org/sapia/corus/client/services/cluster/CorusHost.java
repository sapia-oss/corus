package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Strings;

/**
 * Holds misc information about a Corus host.
 * 
 * @author J-C Desrochers
 * @author yduchesne
 */
public class CorusHost implements Externalizable, JsonStreamable, Matcheable, Mappable {

  /**
   * Indicates the role of the Corus node corresponding to this instance.
   */
  public enum RepoRole {

    /**
     * Indicates this node is neither a client or server repository node.
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
  
  // ==========================================================================

  static final long serialVersionUID = 1L;

  public static String localHostName;
  public static final String UNDEFINED_HOSTNAME = "N/A";

  private String   node;
  private Endpoint endpoint;
  private String   osInfo;
  private String   javaVmInfo;
  private RepoRole role        = RepoRole.NONE;
  private String   hostName;
  private long     creationTime = System.currentTimeMillis();
  private PublicKey  publicKey;

  static {
    try {
      localHostName = Localhost.getPreferredLocalAddress().getHostName();
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
   * @param endpoint
   *          the {@link Endpoint} of the Corus node to which the new
   *          {@link CorusHost} will correspond.
   * @param anOsInfo
   *          the OS info.
   * @param aJavaVmInfo
   *          the Java VM info.
   * @return a new {@link CorusHost}.
   */
  public static CorusHost newInstance(String node, Endpoint endpoint, String anOsInfo, String aJavaVmInfo, PublicKey pubKey) {
    CorusHost created = new CorusHost();
    created.node = node;
    created.endpoint = endpoint;
    created.osInfo = anOsInfo;
    created.javaVmInfo = aJavaVmInfo;
    created.hostName = localHostName;
    created.publicKey = pubKey;
    return created;
  }
  
  /**
   * @return this instance's node identifier.
   */
  public String getNode() {
    return node;
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
   * @param aOsInfo
   *          The new value of the osInfo attribute.
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
   * @param aJavaVmInfo
   *          The new value of the javaVmInfo attribute.
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
   * @param hostName
   *          a host name.
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
   * @return the {@link PublicKey} of the Corus server corresponding to this instance.
   */
  public PublicKey getPublicKey() {
    return publicKey;
  }
  
  /**
   * @return the time at which this instance was created, in millis.
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * @return a pretty-print address corresponding to this instance, meant for
   *         display to users.
   */
  public String getFormattedAddress() {
    return hostName.equals(CorusHost.UNDEFINED_HOSTNAME) ? 
        endpoint.getServerTcpAddress().toString() : 
        hostName + ":" + endpoint.getServerTcpAddress().getPort();
  }
  
  /**
   * @return the a {@link Comparator} that compares {@link CorusHost} instances based on their host name.
   */
  public static Comparator<CorusHost> getHostNameComparator() {
    return new Comparator<CorusHost>() {
      @Override
      public int compare(CorusHost o1, CorusHost o2) {
        return o1.hostName.compareTo(o2.hostName);
      }
    };
  }
  
  @Override
  public boolean matches(Pattern pattern) {
    return pattern.matches(hostName) || 
        pattern.matches(javaVmInfo) ||
        pattern.matches(osInfo) ||
        pattern.matches(osInfo) ||
        pattern.matches(role.name().toLowerCase()) ||
        pattern.matches(this.endpoint.getChannelAddress().toString());
  }
  
  // --------------------------------------------------------------------------
  // Mappable
  
  public java.util.Map<String,Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("host.name", hostName);
    toReturn.put("host.jvmInfo", javaVmInfo);
    toReturn.put("host.osInfo", osInfo);
    toReturn.put("host.address", endpoint.getServerTcpAddress().getHost());
    toReturn.put("host.port", endpoint.getServerTcpAddress().getPort());
    toReturn.put("host.repoRole", role.name());
    return toReturn;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject();
    stream
      .field("hostName").value(hostName)
      .field("host").value(endpoint.getServerTcpAddress().getHost())
      .field("port").value(endpoint.getServerTcpAddress().getPort());
    if (level.greaterThan(ContentLevel.MINIMAL)) {
      stream.field("jvmInfo").value(javaVmInfo)
      .field("osInfo").value(osInfo)
      .field("repoRole").value(role.name());
    }
    stream.endObject();
  }
  
  // --------------------------------------------------------------------------
  // Externalization

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.node = in.readUTF();
    this.endpoint = (Endpoint) in.readObject();
    this.osInfo = (String) in.readObject();
    this.javaVmInfo = (String) in.readObject();
    this.role = (RepoRole) in.readObject();
    this.hostName = (String) in.readObject();
    this.publicKey = (PublicKey) in.readObject();
    this.creationTime = in.readLong();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(node);
    out.writeObject(endpoint);
    out.writeObject(osInfo);
    out.writeObject(javaVmInfo);
    out.writeObject(role);
    out.writeObject(hostName);
    out.writeObject(publicKey);
    out.writeLong(creationTime);
  }

  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public int hashCode() {
    return endpoint.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CorusHost) {
      CorusHost host = (CorusHost) obj;
      return endpoint.equals(host.getEndpoint());
    }
    return false;

  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "endpoint", endpoint, "hostName", hostName, "os", osInfo, "jvm", javaVmInfo, "role", role);
  }

}
