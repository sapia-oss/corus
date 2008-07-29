package org.sapia.corus.processor;

import org.sapia.console.CmdLine;
import org.sapia.taskman.TaskContext;

import java.io.File;
import java.io.IOException;


/**
 * Specifies the behavior for launching native external processes.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface NativeProcess {
  /**
   * Executes the process corresponding to the given command-line and returns
   * the PID of the started process. If the PID could not be returned, then
   * <code>null</code> should be returned.
   *
   * @param ctx The task context of the execution.
   * @param baseDir the base directory of the process to execute.
   * @param cmd the <code>CmdLine</code> to execute.
   * @return the started process' PID, or <code>null</code> if a native PID
   * could not be returned.
   */
  public String exec(TaskContext ctx, File baseDir, CmdLine cmd) throws IOException;

  /**
   * Kills the process corresponding to the given identifier.
   *
   * @param ctx The task context of the kill.
   * @param pid a process' OS-specific identifier.
   * @throws IOException
   */
  public void kill(TaskContext ctx, String pid) throws IOException;
}
