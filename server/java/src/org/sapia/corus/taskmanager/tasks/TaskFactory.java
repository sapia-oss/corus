package org.sapia.corus.taskmanager.tasks;

import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;

import org.sapia.corus.taskmanager.AntTaskHelper;
import org.sapia.taskman.Task;

import java.io.File;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TaskFactory {
  public static Task newUnjarTask(File srcJar, File destDir) {
    Expand unzip = new Expand();
    unzip.setSrc(srcJar);
    unzip.setDest(destDir);

    return AntTaskHelper.init(unzip);
  }

  public static Task newDeleteFileTask(File toDelete) {
    Delete del = new Delete();
    del.setFile(toDelete);

    return AntTaskHelper.init(del);
  }

  public static Task newDeleteDirTask(File toDelete) {
    Delete del = new Delete();
    del.setDir(toDelete);
    del.setIncludeEmptyDirs(true);

    return AntTaskHelper.init(del);
  }

}
