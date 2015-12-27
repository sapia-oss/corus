package org.sapia.corus.docker;

import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.docker.DockerClientException;
import org.sapia.corus.client.services.docker.DockerContainer;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.client.services.docker.DockerManager;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.log.LogCallbackTaskLog;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link DockerManager} interface.
 * 
 * @author yduchesne
 */
@Bind(moduleInterface = { DockerManager.class })
@Remote(interfaces = DockerManager.class)
public class DockerManagerImpl extends ModuleHelper implements DockerManager {

  @Autowired
  private DockerFacade dockerFacade;
  
  @Autowired
  private TaskManager  taskMan;
  
  // --------------------------------------------------------------------------
  // Provided for testing
  
  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }
  
  public void setTaskMan(TaskManager taskMan) {
    this.taskMan = taskMan;
  }
  
  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ROLE;
  }

  // --------------------------------------------------------------------------
  // Life-cycle
  
  @Override
  public void init() throws Exception {
    
  }
  
  @Override
  public void dispose() throws Exception {
    
  }
  
  // --------------------------------------------------------------------------
  // DockerManager interface
  
  @Override
  public List<DockerImage> getImages(ArgMatcher tagMatcher) throws DockerClientException {
    try {
      return dockerFacade.getDockerClient().listImages(tagMatcher);
    } catch (DockerFacadeException e) {
      throw new DockerClientException("Docker error occurred (check server logs for full details): " + e.getMessage());
    }
  }
  
  @Override
  public List<DockerContainer> getContainers(ArgMatcher nameMatcher) throws DockerClientException {
    try {
      return dockerFacade.getDockerClient().listContainers(nameMatcher);
    } catch (DockerFacadeException e) {
      throw new DockerClientException("Docker error occurred (check server logs for full details): " + e.getMessage());
    }
  }
  
  @Override
  public ProgressQueue removeImages(final ArgMatcher tagMatcher) {
    final ProgressQueue progress = new ProgressQueueImpl();
    taskMan.execute(new Task<Void, Void>("RemoveDockerImageTask") {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
        try {
          ctx.info("Starting removal of Docker image(s)");
          DockerClientFacade dockerClient = dockerFacade.getDockerClient();
          List<DockerImage> toRemove      = dockerClient.listImages(tagMatcher);
          for (DockerImage i : toRemove) {
            ctx.info("Removing image: " + ToStringUtil.toString(i));
            dockerClient.removeImage(i.getId(), new LogCallbackTaskLog(ctx));
          }
          ctx.info("Removal of image(s) completed");
        } catch (DockerFacadeException e) {
          ctx.error("Docker error occurred (check server logs for full details): " + e.getMessage());
        }
        return null;
      }
    }, null, SequentialTaskConfig.create(new TaskLogProgressQueue(progress)));
    return progress;
  }
  
  @Override
  public ProgressQueue pullImage(final String imageName) {
    final ProgressQueue progress = new ProgressQueueImpl();
    taskMan.execute(new Task<Void, Void>("PullDockerImageTask") {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
        try {
          DockerClientFacade dockerClient = dockerFacade.getDockerClient();
          if (dockerClient.containsImage(imageName)) {
            ctx.warn("Image '" + imageName  + "' already present in Docker daemon (pull will not be performed)");
          } else {
            ctx.info("Starting pull of Docker image: " + imageName);
            dockerClient.pullImage(imageName, new LogCallbackTaskLog(ctx));
            ctx.info("Pull of image completed");
          }
        } catch (DockerFacadeException e) {
          ctx.error("Docker error occurred (check server logs for full details): " + e.getMessage());
        }
        return null;
      }
    }, null, SequentialTaskConfig.create(new TaskLogProgressQueue(progress)));
    return progress;

  }
}
