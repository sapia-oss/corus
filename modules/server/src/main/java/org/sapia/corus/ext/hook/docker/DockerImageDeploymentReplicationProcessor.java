package org.sapia.corus.ext.hook.docker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Starter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.ImageDeploymentReplicationProcessor;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Func;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Docker-specific implementation of the {@link ImageDeploymentReplicationProcessor} interface: creates deployment
 * task for replicating Docker images to Corus repo clients.
 * 
 * @author yduchesne
 *
 */
public class DockerImageDeploymentReplicationProcessor implements ImageDeploymentReplicationProcessor {
  
  private static final int BUFSZ = 8092;
  
  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  
  @Autowired
  private DeployerConfiguration configuration;
  
  @Autowired
  private DockerFacade dockerFacade;
  
  // Not set in a normal context (only set in the context of unit testing)
  private OptionalValue<Func<DeployOutputStream, PairTuple<DeploymentMetadata, ServerAddress>>> deployOutputStreamFunc =
        OptionalValue.none();

  // --------------------------------------------------------------------------
  // Visible for testing
    
  void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }
  
  void setConfiguration(DeployerConfiguration configuration) {
    this.configuration = configuration;
  }
  
  void setDeployOutputStreamFunc(Func<DeployOutputStream, PairTuple<DeploymentMetadata, ServerAddress>> func) {
    this.deployOutputStreamFunc = OptionalValue.of(func);
  }
  
  // --------------------------------------------------------------------------
  // ImageDeploymentReplication interface
  
  @Override
  public boolean accepts(DeploymentContext context) {
    for (ProcessConfig pc : context.getDistribution().getProcesses()) {
      for (Starter st : pc.getStarters()) {
        if (st instanceof DockerStarter) {
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public synchronized Task<Void, Void> getImageDeploymentTaskFor(DeploymentContext context, List<Endpoint> endpoints) {
    CompositeTask tasks = new CompositeTask();
    for (ProcessConfig pc : context.getDistribution().getProcesses()) {
      for (Starter st : pc.getStarters()) {
        if (st instanceof DockerStarter) {
          DockerStarter dst = (DockerStarter) st;
          String imageName;
          if (dst.getImage().isSet()) {
            imageName = dst.getImage().get();
          } else {
            imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
          }
          log.debug("Will replicate Docker image " + imageName + " for distribution: " + ToStringUtil.toString(context.getDistribution()));
          String imageFileName = "docker_" + imageName.replace(':', '_').replace('/', '_') + ".tar";
          File imageDir = FilePath.newInstance().addDir(configuration.getRepoDir()).createFile();
          if (!imageDir.exists()) {
            imageDir.mkdirs();
          }
          File imageFile = new File(imageDir, imageFileName);
          transferImageLocally(imageName, imageFile);
          DockerImageRequestHandlerTask task = new DockerImageRequestHandlerTask(imageName, imageFile, endpoints);
          if (deployOutputStreamFunc.isSet()) {
            task.setDeployOutputStreamFunc(deployOutputStreamFunc.get());
          }
          tasks.add(task, TimeUnit.SECONDS.toMillis(configuration.getDeploymentTaskTimeoutSeconds()));
        }
      }
    }
    return tasks;
  }
  
  private void transferImageLocally(String imageName, File imageFile) {
    if (!imageFile.exists()) {
      log.debug(String.format("Transferring image %s locally from Docker daemon to: %s", imageName, imageFile));
      final OptionalValue<String>            errorMsg = OptionalValue.none();
      final Reference<OptionalValue<String>> errorRef = new DefaultReference<>(errorMsg);
      LogCallback callback = new LogCallback() {
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
      };
      try (InputStream is = new BufferedInputStream(dockerFacade.getDockerClient().saveImage(imageName, callback))) {
        try (FileOutputStream os = new FileOutputStream(imageFile)) {
          IOUtil.transfer(is, os, BUFSZ);
          os.flush();
        }
      } catch (IOException e) {
        throw new IllegalStateException("Could not transfer image locally from Docker daemon", e);
      }
      
      if (errorRef.get().isSet()) {
        throw new IllegalStateException("Problem occurred while transferring image locally from Docker daemon: " + errorRef.get().get());
      }
    }
  }

}
