package org.sapia.corus.deployer.task;

import java.io.File;
import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.deployer.event.UndeploymentEvent;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.tasks.TaskFactory;


/**
 * This tasks remove a distribution from the corus server.
 * 
 * @author Yanick Duchesne
 */
public class UndeployTask extends Task {
  private Arg      _name;
  private Arg      _version;
  private DistributionDatabase _store;

  public UndeployTask(DistributionDatabase store, Arg name, Arg version) {
    _store   = store;
    _name    = name;
    _version = version;
  }

  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    List<Distribution> dists    = _store.getDistributions(_name, _version);
    for(Distribution dist:dists){
      File         distDir = new File(dist.getBaseDir());
      ctx.info("Undeploying distribution " + _name + ", version: " +
                 _version);
      Task deleteDir = TaskFactory.newDeleteDirTask(distDir);
      ctx.getTaskManager().executeAndWait(deleteDir).get();
      _store.removeDistribution(_name, _version);
      ctx.info("Undeployment successful");
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new UndeploymentEvent(dist));
    }
    return null;
  }
}
