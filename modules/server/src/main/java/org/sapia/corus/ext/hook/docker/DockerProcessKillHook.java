package org.sapia.corus.ext.hook.docker;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessKillHook;
import org.springframework.beans.factory.annotation.Autowired;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

/**
 * Implements the {@link ProcessKillHook} interface in the context of Docker.
 * 
 * @author yduchesne
 *
 */
public class DockerProcessKillHook implements ProcessKillHook {
  
  @Autowired
  private DockerFacade dockerFacade;
  
  @Autowired
  private ProcessorConfiguration processorConfig;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }
  
  public void setProcessorConfig(ProcessorConfiguration processorConfig) {
    this.processorConfig = processorConfig;
  }
  
  // --------------------------------------------------------------------------
  // ProcessKillHook interface
  
  @Override
  public boolean accepts(ProcessContext context) {
    return context.getProcess().getStarterType().equals(StarterType.DOCKER);
  }
  
  @Override
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException {
    if (context.getProcess().getStatus() != LifeCycleStatus.KILL_CONFIRMED) {
      DockerClient client = dockerFacade.getDockerClient();
      try {
        callback.info(String.format("Terminating Docker containerfor process %s", ToStringUtils.toString(context.getProcess())));
        int secondsToWaitBeforeHardKill = 
            (int) TimeUnit.MILLISECONDS.toSeconds(processorConfig.getKillIntervalMillis()) * context.getProcess().getMaxKillRetry();
        client.stopContainer(context.getProcess().getOsPid(), secondsToWaitBeforeHardKill);
        context.getProcess().setStatus(LifeCycleStatus.KILL_CONFIRMED);
      } catch (InterruptedException | DockerException e) {
        throw new IOException("Could not start Docker container for process: " + ToStringUtils.toString(context.getProcess()), e);
      } finally {
        client.close();
      }
    }
  }
  

}
