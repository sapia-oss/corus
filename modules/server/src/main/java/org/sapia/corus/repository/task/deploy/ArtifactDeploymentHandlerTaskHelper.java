package org.sapia.corus.repository.task.deploy;

import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.CompositeTask;

/**
 * An instance of this class is expected to create the subtasks that are
 * necessary in the context of deploying an artifact (distribution, file, shell
 * script).
 * 
 * @author yduchesne
 * 
 */
public abstract class ArtifactDeploymentHandlerTaskHelper {

  private RepositoryConfiguration config;
  private TaskExecutionContext context;

  /**
   * @param config
   *          the {@link RepositoryConfiguration}.
   * @param context
   *          the {@link TaskExecutionContext} in the context of which this
   *          instance will be used.
   */
  protected ArtifactDeploymentHandlerTaskHelper(RepositoryConfiguration config, TaskExecutionContext context) {
    this.config = config;
    this.context = context;
  }

  /**
   * @return the {@link RepositoryConfiguration}.
   */
  protected RepositoryConfiguration config() {
    return config;
  }

  /**
   * @return the {@link TaskExecutionContext} of the task in the context of
   *         which this instance is used.
   */
  protected TaskExecutionContext context() {
    return context;
  }

  /**
   * Adds any relevant deployment subtask to the given composite task.
   * 
   * @param tasks
   *          a {@link CompositeTask}.
   */
  public abstract void addTo(CompositeTask tasks);
}
