package org.sapia.corus.deployer;

import org.sapia.corus.LogicException;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.taskmanager.tasks.TaskFactory;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;

import java.io.File;


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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeployTask implements Task {
  private String            _fName;
  private String            _tmpDir;
  private String            _deployDir;
  private DistributionStore _store;

  DeployTask(DistributionStore store, String fileName, String tmpDir,
             String deployDir) {
    _fName     = fileName;
    _tmpDir    = tmpDir;
    _deployDir = deployDir;
    _store     = store;
  }

  /**
   * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    try {
    	
      int idx = _fName.lastIndexOf('.');

      if (idx < 0) {
        ctx.getTaskOutput().error("File name does not have a temporary extension: " + _fName);

        return;
      }

      String shortName = _fName.substring(0, idx);
      File   src = new File(_tmpDir + File.separator + _fName);
      ctx.getTaskOutput().info("Deploying: " + shortName);

      // extraction corus.xml from archive and checking if already exists...
      Distribution dist    = Distribution.newInstance(src.getAbsolutePath());
      String       baseDir = _deployDir + File.separator + dist.getName() +
                             File.separator + dist.getVersion();
      dist.setBaseDir(baseDir);

      synchronized (_store) {
        File dest = new File(baseDir + File.separator + "common");
        File vms = new File(baseDir + File.separator + "processes");

        if (_store.containsDistribution(dist.getName(), dist.getVersion())) {
          ctx.getTaskOutput().error(new LogicException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return;
        }

        if (dest.exists()) {
          ctx.getTaskOutput().error(new LogicException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return;
        }

        // making distribution directories...
        if (!dest.exists() && !dest.mkdirs()) {
          ctx.getTaskOutput().error("Could not make directory: " + dest.getAbsolutePath());
        }

        vms.mkdirs();

        Task unjar = TaskFactory.newUnjarTask(src, dest);
        //ctx.execSyncNestedTask("UnjarTask", unjar);
        unjar.exec(ctx);

        try {
          _store.addDistribution(dist);
          ctx.getTaskOutput().info("Distribution added to Corus");
        } catch (LogicException e) {
          // noop
        }
      }
    } catch (DeploymentException e) {
      ctx.getTaskOutput().error(e);
    } finally {
      Task deleteFile = TaskFactory.newDeleteFileTask(new File(_tmpDir +
                                                               File.separator +
                                                               _fName));
      //ctx.execSyncNestedTask("DeleteFileTask", deleteFile);
      deleteFile.exec(ctx);
      ctx.getTaskOutput().info("Deployment completed");
      ctx.getTaskOutput().close();
    }
  }
}
