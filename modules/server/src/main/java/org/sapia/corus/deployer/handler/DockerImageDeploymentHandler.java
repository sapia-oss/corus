package org.sapia.corus.deployer.handler;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata.Type;
import org.sapia.corus.client.services.deployer.transport.DockerImageDeploymentMetadata;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles Docker image deployment.
 * 
 * @author yduchesne
 * 
 */
public class DockerImageDeploymentHandler extends DeploymentHandlerSupport {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  @Autowired
  private DeployerConfiguration configuration;

  @Autowired
  private DockerFacade dockerFacade;
  
  @Autowired
  private TaskManager taskManager;

  // --------------------------------------------------------------------------
  // Provided for testing
  
  public void setConfiguration(DeployerConfiguration configuration) {
    this.configuration = configuration;
  }
  
  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }
  
  public void setTaskManager(TaskManager taskManager) {
    this.taskManager = taskManager;
  }
 
  // --------------------------------------------------------------------------
  // DeploymentHandler interface

  @Override
  public boolean accepts(DeploymentMetadata meta) {
    return meta.getType() == Type.DOCKER_IMAGE;
  }

  @Override
  public File getDestFile(DeploymentMetadata meta) {
    return FilePath.newInstance().addDir(configuration.getTempDir()).setRelativeFile(meta.getFileName() + "." + IDGenerator.makeSequentialId()).createFile();
  }

  @Override
  public ProgressQueue completeDeployment(DeploymentMetadata meta, File file) {
    log.info("Finished uploading " + meta.getFileName());
    ProgressQueue progress = new ProgressQueueImpl();
    progress.info("Docker image file uploaded, proceeding to deployment completion");
    
    File srcTar = FilePath.newInstance()
        .addDir(configuration.getTempDir())
        .setRelativeFile(file.getName()).createFile();
    
    if (meta.getPreferences().getChecksum().isSet()) {
      ChecksumPreference cs = meta.getPreferences().getChecksum().get();
      if (!computeChecksum(progress, cs, srcTar)) {
        srcTar.delete();
        return progress;
      }
    }
    
    DockerImageDeploymentMetadata dockerImageMetadata = (DockerImageDeploymentMetadata) meta;
    DockerClientFacade dockerClient = dockerFacade.getDockerClient();
    try {
      if (dockerClient.containsImage(dockerImageMetadata.getImageName())) {
        progress.warning("Image '" +  dockerImageMetadata.getImageName() + "' already present in Docker daemon (loading will not be performed)");
        progress.close();
      } else {
        log.debug("Image '" +  dockerImageMetadata.getImageName() + "' NOT already present in Docker daemon (loading will be performed)");
        doDeployment(progress, dockerImageMetadata, srcTar);
      }
    } catch (Exception e) {
      log.error("Could not load Docker image: " + dockerImageMetadata.getImageName(), e);
      progress.error(e);
      progress.close();
    } 
    return progress;
  }
  
  private void doDeployment(ProgressQueue progress, final DockerImageDeploymentMetadata dockerImageMetadata, final File srcTar) throws Exception {
    
    SequentialTaskConfig taskConfig = SequentialTaskConfig.create(new TaskLogProgressQueue(progress));
    
    Task<Void, Void> task = new Task<Void, Void>("LoadDockerImageTask") {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
        ctx.info(String.format("Starting loading of image %s into Docker daemon", dockerImageMetadata.getImageName()));
        try(FileInputStream is = new FileInputStream(srcTar)) {
          
          final OptionalValue<String>            errorMsg = OptionalValue.none();
          final Reference<OptionalValue<String>> errorRef = new DefaultReference<>(errorMsg);
          dockerFacade.getDockerClient().loadImage(dockerImageMetadata.getImageName(), new FileInputStream(srcTar), new LogCallback() {
            @Override
            public void info(String msg) {
              log.info(msg);
            }
            
            @Override
            public void error(String msg) {
              log.error(msg);
              errorRef.set(OptionalValue.of(msg));
            }
            
            @Override
            public void debug(String msg) {
              log.debug(msg);
            }
          });
          if (errorRef.get().isSet()) {
            throw new IllegalStateException("Error occurred while performing Docker image deployment: " + errorRef.get().get());
          }
          ctx.info(String.format("Finished loading image", dockerImageMetadata.getImageName()));
        }
        return null;
      }
    };
    taskManager.execute(task, null, taskConfig);
    
  }

}
