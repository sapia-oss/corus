package org.sapia.corus.os;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.sapia.console.CmdLine;
import org.sapia.console.ExecHandle;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.os.OsModule.LogCallback;
import org.sapia.corus.util.IOUtil;

/**
 * Unix implementation of the {@link NativeProcess} interface.
 * 
 * @author Yanick Duchesne
 * 
 */
public class UnixProcess implements NativeProcess {

  private static int BUFSZ = 1024;
  private static int COMMAND_TIME_OUT = 5000;

  @Override
  public String exec(LogCallback log, File baseDir, CmdLine cmd) throws IOException {
    // Generate the call to the javastart.sh script
    CmdLine javaCmd = new CmdLine();
    String cmdStr = System.getProperty("corus.home") + File.separator + "bin" + File.separator + "javastart.sh";
    if (!new File(cmdStr).exists()) {
      throw new IOException("Executable not found: " + cmdStr);
    }
    javaCmd.addArg("sh").addArg(cmdStr);

    // Add the option of sending the process output to a file
    File processOutputFile = new File(baseDir, "process.out");
    javaCmd.addOpt("o", processOutputFile.getAbsolutePath());

    // Adding the rest of the command
    while (cmd.hasNext()) {
      javaCmd.addElement(cmd.next());
    }

    if (!baseDir.exists()) {
      throw new IOException("Process directory does not exist: " + baseDir.getAbsolutePath());
    }

    // Execute the command to start the process
    ExecHandle vmHandle = javaCmd.exec(baseDir, null);

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtils.extractUntilAvailable(vmHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8").trim());

    // Extract the process id
    String anOsPid = anOutput.toString().trim();
    StringTokenizer st = new StringTokenizer(anOsPid);
    if (st.hasMoreElements()) {
      anOsPid = (String) st.nextElement();
      log.debug("Got PID from process output: " + anOsPid);
    }

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(vmHandle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error starting the process: " + anOutput.toString("UTF-8").trim());
    }

    return anOsPid;
  }

  @Override
  public void kill(LogCallback log, KillSignal sig, String pid) throws IOException {
    // Generate the kill command
    CmdLine aKillCommand = CmdLine.parse("kill -" + sig.code() + " " + pid);

    // Execute the kill command
    log.debug("--> Executing: " + aKillCommand.toString());
    ExecHandle handle = aKillCommand.exec();

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtils.extractUntilAvailable(handle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8").trim());

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(handle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error killing the process: " + anOutput.toString("UTF-8").trim());
    }
  }
}
