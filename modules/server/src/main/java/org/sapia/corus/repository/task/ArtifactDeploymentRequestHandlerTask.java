package org.sapia.corus.repository.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.sapia.corus.util.Queue;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Function;

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
  public static final ThrottleKey DEPLOY_REQUEST_THROTTLE = new DefaultThrottleKey();

  private RepositoryConfiguration repoConfig;
  private Queue<ArtifactDeploymentRequest> deployRequestQueue;

  /**
   * @param repoConfig
   *          the {@link RepositoryConfiguration}.
   * @param deployRequestQueue
   *          a {@link Queue} of pending {@link ArtifactDeploymentRequest}.
   */
  public ArtifactDeploymentRequestHandlerTask(RepositoryConfiguration repoConfig, Queue<ArtifactDeploymentRequest> deployRequestQueue) {
    super(DEPLOY_REQUEST_THROTTLE);
    this.repoConfig = repoConfig;
    this.deployRequestQueue = deployRequestQueue;
  }

  @Override
  public void run() {

    List<ArtifactDeploymentRequest> requests = deployRequestQueue.removeAll();
    Set<Endpoint> allTargets = Collects.convertAsSet(requests, new Function<Endpoint, ArtifactDeploymentRequest>() {
      public Endpoint call(ArtifactDeploymentRequest req) {
        return req.getEndpoint();
      }
    });

    if (!requests.isEmpty()) {
      PerformDeploymentTask deployTasks = new PerformDeploymentTask();

      deployTasks.add(new SendConfigNotificationTask(repoConfig, allTargets));

      deployTasks.add(new SendPortRangeNotificationTask(repoConfig, allTargets));

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

  public final void setDeployRequestQueue(Queue<ArtifactDeploymentRequest> deployRequestQueue) {
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
