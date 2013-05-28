package org.sapia.corus.repository.task;

import org.sapia.corus.client.services.repository.Repository;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Forces a client pull.
 * 
 * @author yduchesne
 *
 */
public class ForcePullTask extends RunnableTask {
  
  private Repository repo;
  
  /**
   * @param repo the {@link Repository}.
   */
  public ForcePullTask(Repository repo) {
    this.repo = repo;
  }
  
  @Override
  public void run() {
    super.context().debug("Pulling distributions from known repos");
    repo.pull();
  }

}
