package org.sapia.corus.client.services.deployer.transport;

/**
 * Describes a native script that is kept on the Corus server side and can be executed remotely.
 * 
 * @author yduchesne
 *
 */
public class ShellScriptDeploymentMetadata extends DeploymentMetadata {
  
  private static final long serialVersionUID = 1L;
  
  private String alias;
  private String description;
  
  /**
   * @param fileName the script's file name.
   * @param contentLen the script's content length, in bytes.
   * @param clustered if <code>true</code>, indicates that the file shall be deployed across the cluster.
   * @param description the file's description.
   */
  public ShellScriptDeploymentMetadata(String fileName, long contentLen, boolean clustered, String alias, String description) {
    super(fileName, contentLen, clustered, DeploymentMetadata.Type.SCRIPT);
    this.alias       = alias;
    this.description = description;
  }
  
  /**
   * @return the alias of the script to which this instance corresponds.
   */
  public String getAlias() {
    return alias;
  }
  
  /**
   * @return the description of the script to which this instance corresponds.
   */
  public String getDescription() {
    return description;
  }
}
