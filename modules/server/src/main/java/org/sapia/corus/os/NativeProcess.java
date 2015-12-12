package org.sapia.corus.os;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;

/**
 * Specifies the behavior for launching native external processes.
 *
 * @author Yanick Duchesne
 */
public interface NativeProcess {

  /**
   * Executes the process corresponding to the given command-line and returns
   * the PID of the started process. If the PID could not be returned, then
   * <code>null</code> should be returned.
   *
   * @param log
   *          the {@link OsModule#LogCallback} to use for logging activity.
   * @param baseDir
   *          the base directory of the process to execute.
   * @param cmd
   *          the {@link CmdLine} to execute.
   * @param processOption The native process option to use.
   * @return the started process' PID, or <code>null</code> if a native PID
   *         could not be returned.
   */
  public String exec(LogCallback log, File baseDir, CmdLine cmd, Map<String, String> processOptions) throws IOException;

  /**
   * Kills the process corresponding to the given identifier.
   *
   * @param log
   *          the {@link OsModule#LogCallback} to use for logging activity.
   * @param sig
   *          the {@link KillSignal} to use.
   * @param pid
   *          a process' OS-specific identifier.
   * @throws IOException
   */
  public void kill(LogCallback log, KillSignal sig, String pid) throws IOException;
}
