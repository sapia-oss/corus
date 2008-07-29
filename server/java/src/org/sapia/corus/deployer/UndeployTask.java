package org.sapia.corus.deployer;

import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.taskmanager.tasks.TaskFactory;

import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;

import java.io.File;


/**
 * This tasks remove a distribution from the corus server.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UndeployTask implements Task {
  private CommandArg      _name;
  private CommandArg      _version;
  private DistributionStore _store;

  UndeployTask(DistributionStore store, CommandArg name, CommandArg version) {
    _store   = store;
    _name    = name;
    _version = version;
  }

  /**
   * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    try {
      Distribution dist    = _store.getDistribution(_name, _version);
      File         distDir = new File(dist.getBaseDir());
      ctx.getTaskOutput().info("Undeploying distribution " + _name + ", version: " +
                 _version);
      //ctx.execSyncNestedTask("DeleteDirTask", TaskFactory.newDeleteDirTask(distDir) );
      TaskFactory.newDeleteDirTask(distDir).exec(ctx);
      _store.removeDistribution(_name, _version);

      if (!distDir.exists()) {
        ctx.getTaskOutput().info("Undeployment successful");
      } else {
        ctx.getTaskOutput().warning(distDir.getAbsolutePath() +
                      " could not be completely removed");
      }
    } catch (LogicException e) {
      ctx.getTaskOutput().error("Specified distribution does not exist");
    } finally {
      ctx.getTaskOutput().close();
    }
  }
}
