package org.sapia.corus.deployer.task;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.FileFacade;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.ubik.util.SysClock;


/**
 * A background task meant to clean old untouched files from Corus' temp directory.
 * 
 * @author yduchesne
 *
 */
public class CleanTempDirTask extends Task<Void, Void> {
  
  private SysClock clock = SysClock.RealtimeClock.getInstance();

  // --------------------------------------------------------------------------
  // Visible for testing
  
  /**
   * @param clock the {@link SysClock} to use.
   */
  void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  @Override
  public Void execute(TaskExecutionContext ctx, Void param)
      throws Throwable {
 
    DeployerConfiguration conf        = ctx.getServerContext().getServices().getDeployer().getConfiguration();
    FileSystemModule      fs          = ctx.getServerContext().getServices().getFileSystem();
    long                  fileTimeout = TimeUnit.HOURS.toMillis(conf.getTempFileTimeoutHours());
    
    File tmpDir = fs.getFileHandle(conf.getTempDir());
    
    List<File> toDelete = fs.listFiles(tmpDir);
    if (toDelete != null) {
      long currentTime = clock.currentTimeMillis();
      for (File f : toDelete) {
        FileFacade ff = fs.getFileFacade(f);
        if (currentTime - ff.lastModified() >=  fileTimeout && ff.isFile()) {
          ctx.debug("Auto-deleting file: " + ff.getAbsolutePath());
          fs.deleteFile(f);
        }
      }
    }
    return null;
  }

}
