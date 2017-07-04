package org.sapia.corus.repository.task.deploy;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.deployer.InternalShellScriptManager;
import org.sapia.corus.repository.task.ShellScriptRequestHandlerTask;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Creates the subtasks necessary for shell script deployment.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptDeploymentRequestHandlerTaskHelper extends ArtifactDeploymentHandlerTaskHelper {

  private List<ShellScriptDeploymentRequest> requests;

  public ShellScriptDeploymentRequestHandlerTaskHelper(RepositoryConfiguration config, TaskExecutionContext context,
      List<ShellScriptDeploymentRequest> requests) {
    super(config, context);
    this.requests = requests;
  }

  @Override
  public void addTo(CompositeTask toAddTo) {
    InternalShellScriptManager manager = context().getServerContext().lookup(InternalShellScriptManager.class);

    Map<ShellScript, Set<Endpoint>> scriptTargets = getScriptTargets(context(), requests);

    if (!scriptTargets.isEmpty()) {
      context().info(String.format("Got %s scripts to deploy", scriptTargets.size()));
    }
    
    for (final Map.Entry<ShellScript, Set<Endpoint>> entry : scriptTargets.entrySet()) {
      context().info(String.format("Triggering deployment of %s to %s", entry.getKey(), entry.getValue()));
      try {
        RunnableTask task = new ShellScriptRequestHandlerTask(manager.getScriptFile(entry.getKey()), entry.getKey(), new ArrayList<Endpoint>(
            entry.getValue()));
        toAddTo.add(task, TimeUnit.SECONDS.toMillis(config().getArtifactDeploymentRequestWaitTimeoutSeconds()));
      } catch (FileNotFoundException e) {
        context().error("Shell script file not found, bypassing deployment for: " + entry.getKey(), e);
      }
    }
  }

  // --------------------------------------------------------------------------
  // Package visibility for unit testing

  Map<ShellScript, Set<Endpoint>> getScriptTargets(TaskExecutionContext context, List<ShellScriptDeploymentRequest> requests) {
    Map<ShellScript, Set<Endpoint>> scriptTargets = new HashMap<ShellScript, Set<Endpoint>>();
    for (ShellScriptDeploymentRequest req : requests) {
      context.debug("Processing shell script deployment request: " + req);
      for (ShellScript script : req.getScripts()) {
        context.debug(String.format("Adding target %s for deployment of script %s", req.getEndpoint(), script));
        Set<Endpoint> targets = scriptTargets.get(script);
        if (targets == null) {
          targets = new HashSet<Endpoint>();
          scriptTargets.put(script, targets);
        }
        targets.add(req.getEndpoint());
      }
    }
    return scriptTargets;
  }
}
