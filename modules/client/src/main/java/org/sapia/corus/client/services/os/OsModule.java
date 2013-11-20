package org.sapia.corus.client.services.os;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;

/**
 * Specifies OS-level methods.
 * 
 * @author yduchesne
 * 
 */
public interface OsModule {

  public static final String ROLE = OsModule.class.getName();

  /**
   * Callback specified for logging.
   */
  interface LogCallback {
    void debug(String msg);

    void error(String error);
  }

  /**
   * Executes the process specified by the given command-line and returns the
   * corresponding PID.
   * 
   * @param log
   *          a {@link LogCallback}.
   * @param rootDirectory
   *          a {@link File} corresponding to the process' execution directory.
   * @param commandLine
   *          a {@link CmdLine} corresponding to the command-line to execute.
   * @return the native PID of the executed process.
   * @throws IOException
   *           if an error occurred while trying to execute the process.
   */
  public String executeProcess(LogCallback log, File rootDirectory, CmdLine commandLine) throws IOException;

  /**
   * Executes the shell script specified by the given command-line.
   * 
   * @param log
   *          a {@link LogCallback}.
   * @param rootDirectory
   *          a {@link File} corresponding to the script's execution directory.
   * @param commandLine
   *          a {@link CmdLine} corresponding to the script to execute.
   * @throws IOException
   *           if an error occurred while trying to execute the script.
   */
  public void executeScript(LogCallback log, File rootDirectory, CmdLine commandLine) throws IOException;

  /**
   * Kills the process specified by the given PID.
   * 
   * @param log
   *          a {@link LogCallback}.
   * @param pid
   *          the PID of the process to kill.
   * @throws IOException
   *           if an error occurred while trying to kill the process.
   */
  public void killProcess(LogCallback log, String pid) throws IOException;

}
