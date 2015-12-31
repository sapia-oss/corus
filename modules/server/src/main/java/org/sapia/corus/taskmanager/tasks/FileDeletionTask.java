package org.sapia.corus.taskmanager.tasks;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.common.FileFacade;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.ubik.util.SysClock;

/**
 * Reusable task: deletes files under a directory, if they are deemed stale.
 * 
 * @author yduchesne.
 *
 */
public class FileDeletionTask extends Task<Void, Void> {
  
  private SysClock clock = SysClock.RealtimeClock.getInstance();
  
  private File baseDir;
  private long fileTttlMillis;
  
  /**
   * @param taskName the name of the task (used for logging).
   * @param baseDir the {@link File} instance corresponding to the directory to check under.
   * @param fileTtlMillis a time-to-live in milliseconds, beyond which files are automatically deleted.
   */
  public FileDeletionTask(String taskName, File baseDir, long fileTtlMillis) {
    super(taskName);
    this.baseDir        = baseDir;
    this.fileTttlMillis = fileTtlMillis;
  }

  // --------------------------------------------------------------------------
  // Visible for testing
  
  /**
   * @param clock the {@link SysClock} to use.
   */
  void setClock(SysClock clock) {
    this.clock = clock;
  }

  // --------------------------------------------------------------------------
  // Task impl.

  @Override
  public Void execute(TaskExecutionContext ctx, Void param)
      throws Throwable {
 
    FileSystemModule      fs          = ctx.getServerContext().getServices().getFileSystem();
        
    List<File> toDelete = fs.listFiles(baseDir);
    if (toDelete != null) {
      long currentTime = clock.currentTimeMillis();
      for (File f : toDelete) {
        FileFacade ff = fs.getFileFacade(f);
        if (currentTime - ff.lastModified() >=  fileTttlMillis && ff.isFile()) {
          ctx.debug("Auto-deleting file: " + ff.getAbsolutePath());
          fs.deleteFile(f);
        }
      }
    }
    return null;
  }

}
