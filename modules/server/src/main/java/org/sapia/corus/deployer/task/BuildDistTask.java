package org.sapia.corus.deployer.task;

import java.io.File;
import java.io.FileInputStream;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * This task rebuilds the internal distribution objects after a restart.
 * It processes each distribution directory and looks for the corresponding
 * corus.xml files. Each internal distribution object is reconstituted
 * from the information that is contained in the corus.xml file.
 *
 * @author Yanick Duchesne
 */
public class BuildDistTask extends Task {
  private String            _deployDir;
  private DistributionDatabase _store;

  public BuildDistTask(String deployDir, DistributionDatabase store) {
    _deployDir = deployDir;
    _store     = store;
  }

  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    File   f = new File(_deployDir);

    File[] distDirs = f.listFiles();

    if ((distDirs != null) && (distDirs.length > 0)) {
      for (int i = 0; i < distDirs.length; i++) {
        processDistDir(ctx, distDirs[i]);
      }
    } else {
      ctx.info("No distributions");
    }
    return null;
  }

  private void processDistDir(TaskExecutionContext ctx, File distDir) {
    File[] versionDirs = distDir.listFiles();

    if ((versionDirs != null) && (versionDirs.length > 0)) {
      for (int i = 0; i < versionDirs.length; i++) {
        if (versionDirs[i].exists()) {
          processVersionDir(ctx, versionDirs[i]);
        }
      }
    }
  }

  private void processVersionDir(TaskExecutionContext ctx, File versionDir) {
    File corusXML = new File(versionDir.getAbsolutePath() + File.separator +
                              "common" + File.separator + "META-INF" +
                              File.separator + "corus.xml");

    if (corusXML.exists()) {
      try {
        Distribution dist = Distribution.newInstance(new FileInputStream(corusXML));
        dist.setBaseDir(versionDir.getAbsolutePath());
        _store.addDistribution(dist);
        ctx.info("Adding distribution: " + dist.getName() + ", " + dist.getVersion());
      } catch (Exception e) {
        ctx.error(e);
      }     
    } else {
      ctx.error("File " + corusXML.getAbsolutePath() + " does not exist");
    }
  }
}
