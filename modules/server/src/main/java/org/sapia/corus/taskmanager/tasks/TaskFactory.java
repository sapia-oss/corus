package org.sapia.corus.taskmanager.tasks;

import java.io.File;

import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.sapia.corus.taskmanager.AntTaskHelper;
import org.sapia.corus.taskmanager.core.Task;

/**
 * A factory of predefined {@link Task}s.
 * 
 * 
 * @author Yanick Duchesne
 */
public class TaskFactory {

  /**
   * Creates a {@link Task} that unzips the content of an archive to a given
   * directory.
   * 
   * @param srcJar
   *          corresponds to the archive to unzip.
   * @param destDir
   *          the {@link File} corresponding to the directory where to unzip.
   * @return a new {@link Task}.
   */
  public static Task<Void, Void> newUnjarTask(File srcJar, File destDir) {
    Expand unzip = new Expand();
    unzip.setSrc(srcJar);
    unzip.setDest(destDir);

    return AntTaskHelper.init("UnzipTask", unzip);
  }

  /**
   * Creates a {@link Task} that deletes a given file.
   * 
   * @param toDelete
   *          the {@link File} to delete.
   * @return a new {@link Task}.
   */
  public static Task<Void, Void> newDeleteFileTask(File toDelete) {
    Delete del = new Delete();
    del.setFile(toDelete);

    return AntTaskHelper.init("DeleteFileTask", del);
  }

  /**
   * Creates a {@link Task} that deletes a given directory tree.
   * 
   * @param toDelete
   *          the {@link File} corresponding to the directory to delete.
   * @return a new {@link Task}.
   */
  public static Task<Void, Void> newDeleteDirTask(File toDelete) {
    Delete del = new Delete();
    del.setDir(toDelete);
    del.setIncludeEmptyDirs(true);

    return AntTaskHelper.init("DeleteDirTask", del);
  }

}
