package org.sapia.corus.repository.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.repository.task.deploy.ArtifactDeploymentHandlerTaskHelper;
import org.sapia.corus.repository.task.deploy.DistributionDeploymentRequestHandlerTaskHelper;
import org.sapia.corus.repository.task.deploy.FileDeploymentRequestHandlerTaskHelper;
import org.sapia.corus.repository.task.deploy.ShellScriptDeploymentRequestHandlerTaskHelper;
import org.sapia.corus.taskmanager.core.DefaultThrottleKey;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.util.RunnableThrottleableTask;
import org.sapia.corus.util.DelayedQueue;
import org.sapia.corus.util.Queue;
import org.sapia.ubik.net.ThreadInterruptedException;

/**
 * A task that handles {@link ArtifactDeploymentRequest}s.
 * 
 * @author yduchesne
 * 
 */
public class ArtifactDeploymentRequestHandlerTask extends RunnableThrottleableTask {

  /**
   * The {@link ThrottleKey} that this class uses.
   */
  public static final ThrottleKey DEPLOY_REQUEST_THROTTLE = new DefaultThrottleKey("Repository:HandleArtifactDeploymentRequest");

  private RepositoryConfiguration repoConfig;
  private DelayedQueue<ArtifactDeploymentRequest> deployRequestQueue;

  /**
   * @param repoConfig
   *          the {@link RepositoryConfiguration}.
   * @param deployRequestQueue
   *          a {@link Queue} of pending {@link ArtifactDeploymentRequest}.
   */
  public ArtifactDeploymentRequestHandlerTask(RepositoryConfiguration repoConfig, DelayedQueue<ArtifactDeploymentRequest> deployRequestQueue) {
    super(DEPLOY_REQUEST_THROTTLE);
    this.repoConfig         = repoConfig;
    this.deployRequestQueue = deployRequestQueue;
  }

  @Override
  public void run() {
    try {
      doRun();
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    }
  } 
  
  private void doRun() throws InterruptedException {
    context().info("Removing all pending artifact deployment requests from queue");
    List<ArtifactDeploymentRequest> requests = deployRequestQueue.removeAllAfterInactivity(TimeUnit.SECONDS.toMillis(repoConfig.getArtifactDeploymentRequestWaitTimeoutSeconds()));
    if (requests.isEmpty()) {
      context().info("Pending deployment requests have already been consumed, this task will abort");
    } else {
      doDeploy(requests);
    }
  }
  
  private void doDeploy(List<ArtifactDeploymentRequest> requests) {
    context().info(String.format("Got %s artifact deployment requests to process", requests.size()));
    Set<PairTuple<Boolean, Endpoint>> allTargets = requests.stream()
        .map(r -> new PairTuple<Boolean, Endpoint>(r.isForce(), r.getEndpoint()))
        .collect(Collectors.toSet());

    if (!requests.isEmpty()) {
      PerformDeploymentTask deployTasks = new PerformDeploymentTask();

      deployTasks.add(new SendConfigNotificationTask(repoConfig, allTargets),
                      TimeUnit.SECONDS.toMillis(repoConfig.getArtifactDeploymentRequestWaitTimeoutSeconds()));

      deployTasks.add(new SendPortRangeNotificationTask(repoConfig, allTargets),
                      TimeUnit.SECONDS.toMillis(repoConfig.getArtifactDeploymentRequestWaitTimeoutSeconds()));
      
      deployTasks.add(new SendSecurityConfigNotificationTask(repoConfig, allTargets),
                      TimeUnit.SECONDS.toMillis(repoConfig.getArtifactDeploymentRequestWaitTimeoutSeconds()));

      ArtifactDeploymentHandlerTaskHelper distHelper = getDistributionHelper(getDistributionRequests(requests));
      distHelper.addTo(deployTasks);

      ArtifactDeploymentHandlerTaskHelper scriptHelper = getShellScriptHelper(getScriptRequests(requests));
      scriptHelper.addTo(deployTasks);

      ArtifactDeploymentHandlerTaskHelper fileHelper = getFileHelper(getFileRequests(requests));
      fileHelper.addTo(deployTasks);

      context().getTaskManager().execute(deployTasks, null);
    } else {
      context().debug("Nothing to deploy, terminating");
    }
  }

  // ----------------------------------------------------------------------------
  // Provided for testing

  public final void setDeployRequestQueue(DelayedQueue<ArtifactDeploymentRequest> deployRequestQueue) {
    this.deployRequestQueue = deployRequestQueue;
  }

  public final void setRepoConfig(RepositoryConfiguration repoConfig) {
    this.repoConfig = repoConfig;
  }

  ArtifactDeploymentHandlerTaskHelper getDistributionHelper(List<DistributionDeploymentRequest> requests) {
    return new DistributionDeploymentRequestHandlerTaskHelper(repoConfig, context(), requests);
  }

  ArtifactDeploymentHandlerTaskHelper getShellScriptHelper(List<ShellScriptDeploymentRequest> requests) {
    return new ShellScriptDeploymentRequestHandlerTaskHelper(repoConfig, context(), requests);
  }

  ArtifactDeploymentHandlerTaskHelper getFileHelper(List<FileDeploymentRequest> requests) {
    return new FileDeploymentRequestHandlerTaskHelper(repoConfig, context(), requests);
  }

  List<DistributionDeploymentRequest> getDistributionRequests(List<ArtifactDeploymentRequest> requests) {
    List<DistributionDeploymentRequest> distRequests = new ArrayList<DistributionDeploymentRequest>();
    for (ArtifactDeploymentRequest req : requests) {
      if (req instanceof DistributionDeploymentRequest) {
        distRequests.add((DistributionDeploymentRequest) req);
      }
    }
    return distRequests;
  }

  List<ShellScriptDeploymentRequest> getScriptRequests(List<ArtifactDeploymentRequest> requests) {
    List<ShellScriptDeploymentRequest> scriptRequests = new ArrayList<ShellScriptDeploymentRequest>();
    for (ArtifactDeploymentRequest req : requests) {
      if (req instanceof ShellScriptDeploymentRequest) {
        scriptRequests.add((ShellScriptDeploymentRequest) req);
      }
    }
    return scriptRequests;
  }

  List<FileDeploymentRequest> getFileRequests(List<ArtifactDeploymentRequest> requests) {
    List<FileDeploymentRequest> fileRequests = new ArrayList<FileDeploymentRequest>();
    for (ArtifactDeploymentRequest req : requests) {
      if (req instanceof FileDeploymentRequest) {
        fileRequests.add((FileDeploymentRequest) req);
      }
    }
    return fileRequests;
  }
}
