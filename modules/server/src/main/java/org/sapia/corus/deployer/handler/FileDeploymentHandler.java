package org.sapia.corus.deployer.handler;

import java.io.File;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata.Type;
import org.sapia.corus.client.services.deployer.transport.FileDeploymentMetadata;
import org.sapia.corus.deployer.DeploymentHandler;
import org.sapia.corus.util.FilePath;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link DeploymentHandler} for handling arbitrary artifacts.
 * 
 * @author yduchesne
 * 
 */
public class FileDeploymentHandler implements DeploymentHandler {

  @Autowired
  private DeployerConfiguration configuration;

  // --------------------------------------------------------------------------
  // Provided for testing

  public final void setConfiguration(DeployerConfiguration configuration) {
    this.configuration = configuration;
  }

  // --------------------------------------------------------------------------
  // DeploymentHandler interface

  @Override
  public boolean accepts(DeploymentMetadata meta) {
    return meta.getType() == Type.FILE;
  }

  @Override
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File uploadedFile) {
    ProgressQueue progress = new ProgressQueueImpl();
    progress.debug("Completed uploading file to: " + uploadedFile);
    progress.close();
    return progress;
  }

  @Override
  public File getDestFile(DeploymentMetadata meta) {
    FileDeploymentMetadata fileMeta = (FileDeploymentMetadata) meta;
    if (fileMeta.getDirName() == null) {
      return FilePath.newInstance().addDir(configuration.getUploadDir()).setRelativeFile(meta.getFileName()).createFile();
    } else {
      return FilePath.newInstance().addDir(fileMeta.getDirName()).setRelativeFile(meta.getFileName()).createFile();
    }
  }

}
