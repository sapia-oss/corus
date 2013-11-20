package org.sapia.corus.deployer.handler;

import java.io.File;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata.Type;
import org.sapia.corus.deployer.DeploymentHandler;
import org.sapia.corus.deployer.task.DeployTask;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.FilePath;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles distribution deployment.
 * 
 * @author yduchesne
 * 
 */
public class DistributionDeploymentHandler implements DeploymentHandler {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  @Autowired
  private TaskManager taskman;

  @Autowired
  private DeployerConfiguration configuration;

  // --------------------------------------------------------------------------
  // Provided for testing

  public final void setConfiguration(DeployerConfiguration configuration) {
    this.configuration = configuration;
  }

  public final void setTaskman(TaskManager taskman) {
    this.taskman = taskman;
  }

  // --------------------------------------------------------------------------
  // DeploymentHandler interface

  @Override
  public boolean accepts(DeploymentMetadata meta) {
    return meta.getType() == Type.DISTRIBUTION;
  }

  @Override
  public File getDestFile(DeploymentMetadata meta) {
    return FilePath.newInstance().addDir(configuration.getTempDir()).setRelativeFile(meta.getFileName() + "." + IDGenerator.makeId()).createFile();
  }

  @Override
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File file) {
    log.info("Finished uploading " + meta.getFileName());
    ProgressQueue progress = new ProgressQueueImpl();
    progress.info("Distribution file uploaded, proceeding to deployment completion");
    try {
      taskman.executeAndWait(new DeployTask(), file.getName(), TaskConfig.create(new TaskLogProgressQueue(progress))).get();

    } catch (Throwable e) {
      log.error("Could not deploy", e);
      progress.error(e);
      progress.close();
    }
    return progress;
  }

}
