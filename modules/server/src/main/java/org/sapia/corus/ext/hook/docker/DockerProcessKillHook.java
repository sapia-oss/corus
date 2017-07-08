package org.sapia.corus.ext.hook.docker;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.docker.DockerFacadeException;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessKillHook;
import org.sapia.ubik.util.Collects;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link ProcessKillHook} interface in the context of Docker.
 *
 * @author yduchesne
 *
 */
public class DockerProcessKillHook implements ProcessKillHook {
  
  private static final Set<LifeCycleStatus> KILLED_STATUSES = Collects.arrayToSet(LifeCycleStatus.KILL_CONFIRMED, LifeCycleStatus.KILL_ASSUMED);

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
    if (!KILLED_STATUSES.contains(context.getProcess().getStatus())) {
      try {
        int secondsToWaitBeforeHardKill = (int) TimeUnit.MILLISECONDS.toSeconds(
                processorConfig.getKillIntervalMillis()) * context.getProcess().getMaxKillRetry();

        DockerClientFacade client = dockerFacade.getDockerClient();
        
        client.stopContainer(context.getProcess().getOsPid(), secondsToWaitBeforeHardKill, callback);

        client.removeContainer(context.getProcess().getOsPid(), callback);

        context.getProcess().setStatus(LifeCycleStatus.KILL_CONFIRMED);

      } catch (DockerFacadeException e) {
        throw new IOException("Could not kill Docker container for process: " + ToStringUtil.toString(context.getProcess()), e);
      }
    }
  }
}
