package org.sapia.corus.ext.hook.docker;

import java.io.IOException;

import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter.DockerStarterAttachment;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessStartHook;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link ProcessStartHook} interface in the context of docker.
 *
 * @author yduchesne
 *
 */
public class DockerProcessStartHook implements ProcessStartHook {

  @Autowired
  private ServerContext serverContext;

  @Autowired
  private DockerFacade dockerFacade;

  // --------------------------------------------------------------------------
  // Visible for testing

  public void setServerContext(ServerContext serverContext) {
    this.serverContext = serverContext;
  }

  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }

  // --------------------------------------------------------------------------
  // ProcessStartHook interface

  @Override
  public boolean accepts(ProcessContext context) {
    return context.getProcess().getStarterType().equals(StarterType.DOCKER);
  }

  @Override
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException {

    DockerStarterAttachment attachment = starterResult.getNotNull(
        DockerStarter.DOCKER_STARTER_ATTACHMENT,
        DockerStarterAttachment.class
    );
    
    DockerClientFacade client = dockerFacade.getDockerClient();

    String containerId = client.startContainer(
        context, 
        starterResult, 
        attachment, 
        callback
    );
    
    context.getProcess().setOsPid(containerId);
  }

}
