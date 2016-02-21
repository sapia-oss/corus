package org.sapia.corus.client.services.deployer.transport;

import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.util.Assertions;

/**
 * Models meta-information about a given deployment.
 * 
 * @author Yanick Duchesne
 */
public abstract class DeploymentMetadata implements java.io.Serializable {

  /**
   * Indicates the type of artifact being deployed.
   */
  public enum Type {

    /**
     * Indicates that the deployment consists of a distribution.
     */
    DISTRIBUTION,
    /**
     * Indicates that the deployment consists of a shell script.
     */
    SCRIPT,
    /**
     * Indicates that the deployment consists of a file.
     */
    FILE,
    
    /**
     * Indicates that the deployment consists of a Docker image.
     */
    DOCKER_IMAGE
  }

  static final long serialVersionUID = 1L;

  private VmId origin = VmId.getInstance();

  private Set<ServerAddress> visited = new HashSet<ServerAddress>();
  private String fileName;
  private long contentLen;
  private ClusterInfo clusterInfo;
  private Type type;
  private DeployPreferences preferences;
  private OptionalValue<AuditInfo> auditInfo = OptionalValue.none();

  /**
   * @param fileName
   *          the name of the file being deployed.
   * @param contentLen
   *          the length of the file (in bytes).
   * @param cluster
   *          the {@link ClusterInfo} that indicates if deployment should be
   *          clustered or not.
   * @param type
   *          the deployment type.
   * @param prefs
   *          the {@link DeployPreferences} (which may or may not apply for the given deployment type).
   */
  protected DeploymentMetadata(String fileName, long contentLen, ClusterInfo cluster, Type type, DeployPreferences prefs) {
    this.fileName    = fileName;
    this.contentLen  = contentLen;
    this.clusterInfo = cluster;
    this.type        = type;
    this.preferences = prefs;
  }


  /**
   * @return this instance's {@link Type}.
   */
  public Type getType() {
    return type;
  }

  /**
   * @return the file name of the deployed archive.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return the length (in bytes) of the deployed archive.
   */
  public long getContentLength() {
    return contentLen;
  }

  /**
   * @return <code>true</code> if deployment is clustered.
   */
  public boolean isClustered() {
    return clusterInfo.isClustered();
  }

  /**
   * Returns the addresses of the servers to which the deployment has been
   * successfully uploaded.
   * 
   * @return a {@link Set} of {@link ServerAddress} instances.
   */
  public Set<ServerAddress> getVisited() {
    return visited;
  }

  /**
   * @return the {@link ClusterInfo} that this instance wraps.
   */
  public ClusterInfo getClusterInfo() {
    return clusterInfo;
  }

  /**
   * @param addr
   *          a {@link ServerAddress}.
   * @return <code>true</code> if the given address corresponds to a node that
   *         is target by this deployment.
   */
  public boolean isTargeted(ServerAddress addr) {
    return clusterInfo.getTargets().isEmpty() || clusterInfo.getTargets().contains(addr);
  }

  /**
   * @return the {@link VmId} of the server from which this instance originates.
   */
  public VmId getOrigin() {
    return origin;
  }
  
  /**
   * @return this instance's {@link DeployPreferences}.
   */
  public DeployPreferences getPreferences() {
    return preferences;
  }
  
  /**
   * @return the optional {@link AuditInfo}.
   */
  public OptionalValue<AuditInfo> getAuditInfo() {
    return auditInfo;
  }
  
  /**
   * @param auditInfo the {@link AuditInfo} to use.
   */
  public void setAuditInfo(AuditInfo auditInfo) {
    Assertions.illegalState(!auditInfo.isEncrypted(), "AuditInfo must be encrypted");
    this.auditInfo = OptionalValue.of(auditInfo);
  }
}
