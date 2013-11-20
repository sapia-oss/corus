package org.sapia.corus.deployer;

import java.io.File;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;

/**
 * Specifies the behavior for handling incoming deployments.
 * 
 * @author yduchesne
 * 
 */
public interface DeploymentHandler {

  /**
   * @param meta
   *          the {@link DeploymentMetadata} corresponding to the incoming
   *          deployment.
   * @return <code>true</code> if
   */
  public boolean accepts(DeploymentMetadata meta);

  /**
   * @param meta
   *          the {@link DeploymentMetadata} corresponding to the file being
   *          deployed.
   * @return the {@link File} to copy to locally on the Corus node.
   */
  public File getDestFile(DeploymentMetadata meta);

  /**
   * Invoked uploading of the file being deployed has completed. An instance of
   * this class's responsibility is to proceed with completing the deployment as
   * it sees fit.
   * 
   * @param meta
   *          the {@link DeploymentMetadata} of the file that has been uploaded.
   * @param uploadedFile
   *          the full path to the file that was deployed.
   */
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File uploadedFile);

}
