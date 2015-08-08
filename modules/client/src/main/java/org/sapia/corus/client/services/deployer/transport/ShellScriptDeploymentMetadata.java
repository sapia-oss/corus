package org.sapia.corus.client.services.deployer.transport;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.DeployPreferences;

/**
 * Describes a native script that is kept on the Corus server side and can be
 * executed remotely.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptDeploymentMetadata extends DeploymentMetadata {

  private static final long serialVersionUID = 1L;

  private String alias;
  private String description;

  public ShellScriptDeploymentMetadata(String fileName, long contentLen, String alias, String description, DeployPreferences prefs, ClusterInfo cluster) {
    super(fileName, contentLen, cluster, DeploymentMetadata.Type.SCRIPT, prefs);
    this.alias = alias;
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
