package org.sapia.corus.os;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarPermissionDeniedException;
import org.sapia.console.CmdLine;
import org.sapia.console.ExecHandle;
import org.sapia.console.Option;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.sigar.SigarSupplier;
import org.sapia.corus.util.IOUtil;

/**
 * Windows implementation of the {@link NativeProcess} interface.
 *
 * @author Yanick Duchesne
 *
 */
public class WindowsProcess implements NativeProcess {

  private static final long PAUSE_AFTER_START = 1000;
  private static int BUFSZ = 1024;
  private static int COMMAND_TIME_OUT = 5000;

  private static String pvPath;

  /**
   * Utility method that returns a new CmdLine object with the path to the
   * process viewer tool.
   *
   * @return A CmdLine containing the path to the process viewer tool.
   * @throws IOException
   *           If the process viewer tool is not found
   */
  private static synchronized CmdLine createPVCmdLine() throws IOException {
    if (pvPath == null) {
      // Generate the command line to the process viewer tool
      StringBuffer aCommand = new StringBuffer();
      aCommand.append(System.getProperty("corus.home")).append(File.separator).append("bin").append(File.separator).append("win")
          .append(File.separator).append("pv.exe");

      // Validate the presence and accessibility of the process viewer tool
      if (new File(aCommand.toString()).exists()) {
        pvPath = aCommand.toString();
      } else {
        throw new IOException("Unable to find the process viewer tool under path " + aCommand.toString());
      }
    }

    return new CmdLine().addArg(pvPath);
  }

  /**
   * Utility method that extract the pattern matching expression to retrieve the
   * process that contains the variable corus.process.id and corus.server.port.
   *
   * @param cmd
   *          The command line object from which to extract the pattern
   *          expression.
   * @return The pattern matching expression.
   */
  private static String extractPattern(CmdLine cmd) {
    cmd.reset();
    StringBuffer aBuffer = new StringBuffer("\"*");

    while (cmd.hasNext()) {
      if (cmd.isNextOption()) {
        Option anOption = (Option) cmd.next();
        if (anOption.getName().startsWith("Dcorus.server.port=")) {
          aBuffer.append(anOption.getName()).append("*");
        } else if (anOption.getName().startsWith("Dcorus.process.id=")) {
          aBuffer.append(anOption.getName()).append("*");
        }
      } else {
        cmd.next();
      }
    }

    if (aBuffer.length() == 2) {
      throw new IllegalStateException("Unable to generate a pattern to find the process from the cmd: " + cmd.toString());
    }

    return aBuffer.append("\"").toString();
  }

  /**
   * Returns <code>null</code>
   *
   */
  @Override
  public String exec(OsModule.LogCallback log, File baseDir, CmdLine cmd) throws IOException {
    // Generate the call to the javastart.bat script
    CmdLine javaCmd = new CmdLine();
    String cmdStr = System.getProperty("corus.home") + File.separator + "bin" + File.separator + "javastart.bat";
    if (!new File(cmdStr).exists()) {
      throw new IOException("Executable not found: " + cmdStr);
    }
    javaCmd.addArg("cmd /C ").addArg(cmdStr);

    // Add the option of sending the process output to a file
    File processOutputFile = new File(baseDir, "process.out");
    javaCmd.addOpt("o", processOutputFile.getAbsolutePath());
    javaCmd.addArg("\"");

    // Adding the rest of the command
    while (cmd.hasNext()) {
      javaCmd.addElement(cmd.next());
    }
    javaCmd.addArg("\"");

    log.debug(javaCmd.toString());

    if (!baseDir.exists()) {
      throw new IOException("Process directory does not exist: " + baseDir.getAbsolutePath());
    }
    // Execute the command to start the process
    ExecHandle vmHandle = cmd.exec(baseDir, null);

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtils.extractUntilAvailable(vmHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8"));

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(vmHandle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error starting the process: " + anOutput.toString("UTF-8"));
    }

    try {
      Thread.sleep(PAUSE_AFTER_START);
    } catch (InterruptedException e) {
      throw new IOException("Thread was interrupted while pausing after process exec", e);
    }

    if (SigarSupplier.isSet()) {
      return extractPidUsingSigar(log, baseDir);
    // using PV as fallback
    } else {
      return extractPidUsingPV(log, javaCmd, anOutput, baseDir);
    }
  }

  private String extractPidUsingSigar(OsModule.LogCallback log, File baseDir)
    throws IOException {
    try {
      for (long pid : SigarSupplier.get().getProcList()) {
        try {
          ProcExe exe = SigarSupplier.get().getProcExe(pid);
          if (exe.getCwd().toLowerCase().replace("\\", "/")
              .equals(baseDir.getAbsolutePath().toLowerCase().replace("\\", "/"))) {
            log.debug("Got process OS PID: " + pid);
            return Long.toString(pid);
          }
        } catch (SigarPermissionDeniedException e) {
          // noop;
        }

      }
      return null;
    } catch (SigarException e) {
      throw new IOException("Could not obtain process information from Sigar", e);
    }
  }

  private String extractPidUsingPV(OsModule.LogCallback log, CmdLine cmd, ByteArrayOutputStream anOutput, File baseDir)
    throws IOException {
    // Retrieve the OS pid using the process viewer tool
    CmdLine aListCommand = createPVCmdLine();
    aListCommand.addArg("--tree").addArg("-l" + extractPattern(cmd));
    log.debug("--> Executing: " + aListCommand.toString());
    ExecHandle pvHandle = aListCommand.exec(baseDir, null);

    // Extract the output stream of the process
    anOutput.reset();
    CliUtils.extractUntilAvailable(pvHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8"));

    // Generates a string of the format "\njavaw.exe       (284)\n"
    String anOsPid = null;
    String aBuffer = anOutput.toString();
    int start = aBuffer.lastIndexOf("(");
    if (start >= 0) {
      int end = aBuffer.indexOf(")", start);
      anOsPid = aBuffer.substring(start + 1, end);
    }

    if (anOsPid != null) {
      StringTokenizer st = new StringTokenizer(anOsPid);
      if (st.hasMoreElements()) {
        anOsPid = (String) st.nextElement();
        log.debug("Got PID from process output: " + anOsPid);
      }
    }

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(pvHandle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error getting the process id: " + anOutput.toString("UTF-8"));
    }

    return anOsPid;
  }

  @Override
  public void kill(OsModule.LogCallback log, String pid) throws IOException {
    // Generate the kill command
    CmdLine aKillCommand = createPVCmdLine();
    aKillCommand.addOpt("-kill", null).addOpt("-id", pid).addOpt("-force", null);

    // Execute the kill command
    log.debug("--> Executing: " + aKillCommand.toString());
    ExecHandle pvHandle = aKillCommand.exec();

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtils.extractUntilAvailable(pvHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8"));

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(pvHandle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error killing the process: " + anOutput.toString("UTF-8"));
    }
  }
}
