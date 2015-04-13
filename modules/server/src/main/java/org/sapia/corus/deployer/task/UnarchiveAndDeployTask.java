package org.sapia.corus.deployer.task;

import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.deployer.DeployerThrottleKeys;
import org.sapia.corus.deployer.archiver.DistributionArchiver;
import org.sapia.corus.deployer.archiver.DistributionArchiver.DistributionArchive;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * Unarchives the distributions kept under a given revision.
 * 
 * @author yduchesne
 *
 */
public class UnarchiveAndDeployTask extends Task<Void, TaskParams<RevId, Void, Void, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return DeployerThrottleKeys.UNARCHIVE_DISTRIBUTION;
  }
  
  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<RevId, Void, Void, Void> params)
      throws Throwable {
  
    RevId                revId    = params.getParam1();
    DistributionArchiver archiver = ctx.getServerContext().getServices().getDistributionArchiver(); 

    ctx.info("Preparing to unarchive distributions kept under '" + revId + "' revision");
    
    for (DistributionArchive d : archiver.unarchive(revId)) {
      ctx.info("Proceeding to deployment of archived distribution zip: " + d.getDistributionZip().getName());
      DeployTask t = new DeployTask() {
        public ThrottleKey getThrottleKey() {
          return DeployerThrottleKeys.DEPLOY_UNARCHIVED_DISTRIBUTION;
        }
      };
      ctx.getTaskManager().executeAndWait(
        t,
        TaskParams.createFor(
          d.getDistributionZip(), 
          DeployPreferences.newInstance().setExecDeployScripts(false)
        )
      ).get();
    }
    archiver.clear(revId);
    ctx.info("Completed unarchiving of distributions kept under '" + revId + "' revision");
    
    return null;
  }
  


}
