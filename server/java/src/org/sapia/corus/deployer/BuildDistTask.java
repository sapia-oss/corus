package org.sapia.corus.deployer;

import org.sapia.corus.deployer.config.Distribution;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


/**
 * This task rebuilds the internal distribution objects after a restart.
 * It processes each distribution directory and looks for the corresponding
 * corus.xml files. Each internal distribution object is reconstituted
 * from the information that is contained in the corus.xml file.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class BuildDistTask implements Task {
  private String            _deployDir;
  private DistributionStore _store;

  BuildDistTask(String deployDir, DistributionStore store) {
    _deployDir = deployDir;
    _store     = store;
  }

  /**
   * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    try {
      File   f = new File(_deployDir);

      File[] distDirs = f.listFiles();

      if ((distDirs != null) && (distDirs.length > 0)) {
        for (int i = 0; i < distDirs.length; i++) {
          processDistDir(ctx, distDirs[i]);
        }
      } else {
        ctx.getTaskOutput().info("No distributions");
      }
    } finally {
      ctx.getTaskOutput().close();
    }
  }

  private void processDistDir(TaskContext ctx, File distDir) {
    File[] versionDirs = distDir.listFiles();

    if ((versionDirs != null) && (versionDirs.length > 0)) {
      for (int i = 0; i < versionDirs.length; i++) {
        if (versionDirs[i].exists()) {
          processVersionDir(ctx, versionDirs[i]);
        }
      }
    }
  }

  private void processVersionDir(TaskContext ctx, File versionDir) {
    File corusXML = new File(versionDir.getAbsolutePath() + File.separator +
                              "common" + File.separator + "META-INF" +
                              File.separator + "corus.xml");

    if (corusXML.exists()) {
      try {
        Distribution dist = Distribution.newInstance(new FileInputStream(corusXML));
        dist.setBaseDir(versionDir.getAbsolutePath());
        _store.addDistribution(dist);
        ctx.getTaskOutput().info("Adding distribution: " + dist.getName() + ", " + dist.getVersion());
      } catch (FileNotFoundException e) {
        ctx.getTaskOutput().error(e);
      } catch (DeploymentException e) {
        ctx.getTaskOutput().error(e);
      } catch (DuplicateDistributionException e) {
        ctx.getTaskOutput().error(e);
      }
    } else {
      ctx.getTaskOutput().error("File " + corusXML.getAbsolutePath() + " does not exist");
    }
  }
}
