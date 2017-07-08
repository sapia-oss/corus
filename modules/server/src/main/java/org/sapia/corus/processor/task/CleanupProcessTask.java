package org.sapia.corus.processor.task;

import java.io.File;

import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Cleans up the process directory and removes it from the active process list.
 * 
 * @author yduchesne
 * 
 */
public class CleanupProcessTask extends Task<Void, Process> {

  @Override
  public Void execute(TaskExecutionContext ctx, Process proc) throws Throwable {

    FileSystemModule fs = ctx.getServerContext().lookup(FileSystemModule.class);

    if (proc.getStatus() != LifeCycleStatus.KILL_CONFIRMED && proc.getStatus() != LifeCycleStatus.KILL_ASSUMED) {
      throw new IllegalStateException(String.format("Cannot kill process %s; current status is %s, expected: %s or %s", proc, proc.getStatus(),
          LifeCycleStatus.KILL_CONFIRMED, LifeCycleStatus.KILL_ASSUMED));
    }

    if (proc.getProcessDir() != null) {
      if (proc.isDeleteOnKill()) {
        File dir = new File(proc.getProcessDir());
        try {
          fs.deleteDirectory(dir);
          if (fs.exists(dir)) {
            ctx.warn(String.format("Could not destroy process directory: %s", dir.getAbsolutePath()));
          }
        } catch (Throwable e) {
          if (fs.exists(dir)) {
            ctx.warn(String.format("Could not destroy process directory: %s", dir.getAbsolutePath()), e);
          }
        }
      }
    }

    ctx.debug(String.format("Releasing ports for %s", proc));

    ctx.debug(String.format("Removing from active process list: %s", proc));
    proc.delete();
    if (proc.isDeleteOnKill()) {
      ctx.warn(String.format("Process successfully terminated and cleaned up: %s", proc));
    } else {
      ctx.warn(String.format("Process successfully terminated: %s", proc));
    }

    return null;
  }

}
