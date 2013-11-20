package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Strings;

/**
 * Holds misc information about a Corus host.
 * 
 * @author J-C Desrochers
 */
public class ServerHost implements Externalizable {

  /**
   * Indicates the role of the Corus node corresponding to this instance.
   */
  public enum Role {

    /**
     * Indicates a basic node.
     */
    BASIC,

    /**
     * Indicates a repository node.
     */
    REPO;

  }

  static final long serialVersionUID = 1L;

  private ServerAddress serverAddress;
  private String osInfo;
  private String javaVmInfo;
  private Role role = Role.BASIC;

  public static ServerHost createNew(ServerAddress anAddress, String anOsInfo, String aJavaVmInfo) {
    ServerHost created = new ServerHost();
    created.setServerAddress(anAddress);
    created.setOsInfo(anOsInfo);
    created.setJavaVmInfo(aJavaVmInfo);

    return created;
  }

  /**
   * Creates a new {@link ServerHost} instance.
   */
  public ServerHost() {
  }

  /**
   * Returns the serverAddress attribute.
   * 
   * @return The serverAddress value.
   */
  public ServerAddress getServerAddress() {
    return serverAddress;
  }

  /**
   * Changes the value of the attributes serverAddress.
   * 
   * @param aServerAddress
   *          The new value of the serverAddress attribute.
   */
  public void setServerAddress(ServerAddress aServerAddress) {
    serverAddress = aServerAddress;
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
   * @return this instance's {@link Role}.
   */
  public Role getRole() {
    return role;
  }

  /**
   * Sets this instance's {@link Role}.
   * 
   * @param role
   */
  public void setRole(Role role) {
    this.role = role;
  }

  // --------------------------------------------------------------------------
  // Externalization

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.serverAddress = (ServerAddress) in.readObject();
    this.osInfo = (String) in.readObject();
    this.javaVmInfo = (String) in.readObject();
    this.role = (Role) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(serverAddress);
    out.writeObject(osInfo);
    out.writeObject(javaVmInfo);
    out.writeObject(role);
  }

  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public int hashCode() {
    return serverAddress.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ServerHost) {
      ServerHost host = (ServerHost) obj;
      return serverAddress.equals(host.getServerAddress());
    }
    return false;

  }

  @Override
  public String toString() {
    return Strings.toString(this, "address", serverAddress, "os", osInfo, "jvm", javaVmInfo, "role", role);
  }

}
