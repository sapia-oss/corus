package org.sapia.corus.deployer.task;

import java.util.List;

import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.deployer.archiver.DistributionArchiver;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * Performs archival and undeployment of selected distributions.
 *  
 * @author yduchesne
 */
public class UndeployAndArchiveTask extends Task<Void, TaskParams<DistributionCriteria, RevId, Void, Void>> {
  
  @Override
  public Void execute(TaskExecutionContext ctx,
      TaskParams<DistributionCriteria, RevId, Void, Void> params) throws Throwable {
    
    DistributionCriteria criteria  = params.getParam1();
    RevId                revId     = params.getParam2();
    List<Distribution>   toArchive = ctx.getServerContext().getServices().getDeployer().getDistributions(criteria);
    DistributionArchiver archiver  = ctx.getServerContext().getServices().getDistributionArchiver();
    archiver.archive(revId, toArchive);
    try {
      ctx.getTaskManager().executeAndWait(new UndeployTask(), TaskParams.createFor(criteria)).get();
    } catch (Throwable t) {
      archiver.clear(revId);
      throw t;
    }
    return null;
  }

}
