package org.sapia.corus.deployer.handler;

import java.io.File;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata.Type;
import org.sapia.corus.deployer.task.DeployTask;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles distribution deployment.
 * 
 * @author yduchesne
 * 
 */
public class DistributionDeploymentHandler extends DeploymentHandlerSupport {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  @Autowired
  private TaskManager taskman;

  @Autowired
  private Deployer deployer;

  // --------------------------------------------------------------------------
  // Provided for testing

  public final void setTaskman(TaskManager taskman) {
    this.taskman = taskman;
  }
  
  public final void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }

  // --------------------------------------------------------------------------
  // DeploymentHandler interface

  @Override
  public boolean accepts(DeploymentMetadata meta) {
    return meta.getType() == Type.DISTRIBUTION;
  }

  @Override
  public File getDestFile(DeploymentMetadata meta) {
    return FilePath.newInstance().addDir(deployer.getConfiguration().getTempDir()).setRelativeFile(meta.getFileName() + "." + IDGenerator.makeSequentialId()).createFile();
  }

  @Override
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File file) {
    log.info("Finished uploading " + meta.getFileName());
    ProgressQueue progress = new ProgressQueueImpl();
    progress.info("Distribution file uploaded, proceeding to deployment completion");
    
    File srcZip = FilePath.newInstance()
        .addDir(deployer.getConfiguration().getTempDir())
        .setRelativeFile(file.getName()).createFile();
    
    if (meta.getPreferences().getChecksum().isSet()) {
      ChecksumPreference cs = meta.getPreferences().getChecksum().get();
      if (!computeChecksum(progress, cs, srcZip)) {
        srcZip.delete();
        return progress;
      }
    }
    
    try {
   
      taskman.execute(
          new DeployTask(), 
          TaskParams.createFor(srcZip, meta.getPreferences()), 
          SequentialTaskConfig.create(new TaskLogProgressQueue(progress))
      );
    } catch (Throwable e) {
      log.error("Could not deploy", e);
      progress.error(e);
      progress.close();
    }
    return progress;
  }

}
