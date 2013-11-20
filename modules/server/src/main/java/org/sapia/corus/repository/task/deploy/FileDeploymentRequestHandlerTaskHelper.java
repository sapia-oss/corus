package org.sapia.corus.repository.task.deploy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.deployer.InternalFileManager;
import org.sapia.corus.repository.task.FileRequestHandlerTask;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Creates the subtasks pertaining to file deployment.
 * 
 * @author yduchesne
 * 
 */
public class FileDeploymentRequestHandlerTaskHelper extends ArtifactDeploymentHandlerTaskHelper {

  private List<FileDeploymentRequest> requests;

  public FileDeploymentRequestHandlerTaskHelper(RepositoryConfiguration config, TaskExecutionContext context, List<FileDeploymentRequest> requests) {
    super(config, context);
    this.requests = requests;
  }

  @Override
  public void addTo(CompositeTask toAddTo) {
    InternalFileManager manager = context().getServerContext().lookup(InternalFileManager.class);

    Map<FileInfo, Set<Endpoint>> fileTargets = getFileTargets(context(), requests);

    context().info(String.format("Got %s targets to deploy to", fileTargets));

    for (final Map.Entry<FileInfo, Set<Endpoint>> entry : fileTargets.entrySet()) {
      context().info(String.format("Triggering deployment of %s to %s", entry.getKey(), entry.getValue()));
      try {
        RunnableTask task = new FileRequestHandlerTask(manager.getFile(entry.getKey()), new ArrayList<Endpoint>(entry.getValue()));
        toAddTo.add(task);
      } catch (FileNotFoundException e) {
        context().error("File not found, bypassing deployment for: " + entry.getKey().getName(), e);
      }
    }
  }

  // --------------------------------------------------------------------------
  // Package visibility for unit testing

  Map<FileInfo, Set<Endpoint>> getFileTargets(TaskExecutionContext context, List<FileDeploymentRequest> requests) {
    Map<FileInfo, Set<Endpoint>> fileTargets = new HashMap<FileInfo, Set<Endpoint>>();
    for (FileDeploymentRequest req : requests) {
      context.debug("Processing file deployment request: " + req);
      for (FileInfo file : req.getFiles()) {
        context.debug(String.format("Adding target %s for deployment of file %s", req.getEndpoint(), file));
        Set<Endpoint> targets = fileTargets.get(file);
        if (targets == null) {
          targets = new HashSet<Endpoint>();
          fileTargets.put(file, targets);
        }
        targets.add(req.getEndpoint());
      }
    }
    return fileTargets;
  }

}
