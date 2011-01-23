package org.sapia.corus.deployer.task;

import java.io.File;

import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.event.DeploymentEvent;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.tasks.TaskFactory;


/**
 * This task handles the extraction of deployment jars from the temporary
 * file (where they have been uploaded) to the deployment directory.  This
 * task ensures that a distribution will not overwrite an existing one,
 * and cleans up the temporary jar. Distributions are stored under the
 * deployment directory, according to the following pattern:
 * <p>
 * <code>distribution_name/version</code>
 * <p>
 * Each distribution directory has two additional directories:
 * <code>common</code> (where the jar is actually extracted) and,
 * eventually, <code>processes</code>, where each process instance has a specific
 * directory that it owns exclusively.
 *
 *
 * @author Yanick Duchesne
 */
public class DeployTask extends Task {
  private String            _fName;
  private String            _tmpDir;
  private String            _deployDir;
  private DistributionDatabase _store;

  public DeployTask(DistributionDatabase store, String fileName, String tmpDir,
             String deployDir) {
    _fName     = fileName;
    _tmpDir    = tmpDir;
    _deployDir = deployDir;
    _store     = store;
  }

  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    try {
    	
      int idx = _fName.lastIndexOf('.');

      if (idx < 0) {
        ctx.error("File name does not have a temporary extension: " + _fName);

        return null;
      }

      String shortName = _fName.substring(0, idx);
      File   src = new File(_tmpDir + File.separator + _fName);
      ctx.info("Deploying: " + shortName);

      // extraction corus.xml from archive and checking if already exists...
      Distribution dist    = Distribution.newInstance(src.getAbsolutePath());
      String       baseDir = _deployDir + File.separator + dist.getName() +
                             File.separator + dist.getVersion();
      dist.setBaseDir(baseDir);

      synchronized (_store) {
        File dest = new File(baseDir + File.separator + "common");
        File vms = new File(baseDir + File.separator + "processes");

        if (_store.containsDistribution(dist.getName(), dist.getVersion())) {
          ctx.error(new DuplicateDistributionException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return null;
        }

        if (dest.exists()) {
          ctx.error(new DuplicateDistributionException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return null;
        }

        // making distribution directories...
        if (!dest.exists() && !dest.mkdirs()) {
          ctx.error("Could not make directory: " + dest.getAbsolutePath());
        }

        vms.mkdirs();

        Task unjar = TaskFactory.newUnjarTask(src, dest);
        ctx.getTaskManager().executeAndWait(unjar).get();

        try {
          _store.addDistribution(dist);
          ctx.info("Distribution added to Corus");
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new DeploymentEvent(dist));
        } catch (DuplicateDistributionException e) {
          // noop
        }
      }
    } catch (DeploymentException e) {
      ctx.error(e);
    } finally {
      Task deleteFile = TaskFactory.newDeleteFileTask(new File(_tmpDir +
                                                               File.separator +
                                                               _fName));
      ctx.getTaskManager().executeAndWait(deleteFile).get();
    }
    return null;
  }
}
