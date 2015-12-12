package org.sapia.corus.os;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import org.sapia.console.CmdLine;
import org.sapia.console.ExecHandle;
import org.sapia.corus.client.common.CliUtil;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.numa.NumaProcessOptions;

import com.google.common.base.Optional;

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
  public String exec(LogCallback log, File baseDir, CmdLine cmd, Map<String, String> processOptions) throws IOException {
    if (!baseDir.exists()) {
      throw new IOException("Process directory does not exist: " + baseDir.getAbsolutePath());
    }

    // Generate the call to the javastart.sh script
    CmdLine javaCmd = doGenerateJavaCommandLine(baseDir, cmd, processOptions);

    // Execute the command to start the process
    ExecHandle vmHandle = javaCmd.exec(baseDir, null);

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtil.extractUntilAvailable(vmHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
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

  protected CmdLine doGenerateJavaCommandLine(File baseDir, CmdLine cmd, Map<String, String> processOptions) throws IOException {
    // Generate the call to the javastart.sh script
    CmdLine javaCmd = new CmdLine();
    String cmdStr = System.getProperty("corus.home") + File.separator + "bin" + File.separator + "javastart.sh";
    if (!new File(cmdStr).exists()) {
      throw new IOException("Executable not found: " + cmdStr);
    }
    javaCmd.addArg("sh");
    javaCmd.addArg(cmdStr);

    // Add the option of sending the process output to a file
    File processOutputFile = new File(baseDir, "process.out");
    javaCmd.addOpt("o", processOutputFile.getAbsolutePath());

    // Process numa options
    CmdLine numaCmd = doProcessNumaOptions(processOptions);
    while (numaCmd.hasNext()) {
      javaCmd.addElement(numaCmd.next());
    }

    // Adding the rest of the java command
    while (cmd.hasNext()) {
      javaCmd.addElement(cmd.next());
    }

    return javaCmd;
  }

  /**
   * Internal method that looks into the process options passed in for any NUMA related options
   * and generate the appropriate command line.
   *
   * @param processOptions The process options to look into.
   * @return The numa command line to use based on the processing options passed in.
   */
  protected CmdLine doProcessNumaOptions(Map<String, String> processOptions) {
    CmdLine numaCmd = new CmdLine();

    Optional<Integer> numaCoreId = NativeProcessOptions.extractOptionValueAsInteger(NumaProcessOptions.NUMA_CORE_ID, processOptions);
    if (numaCoreId.isPresent()) {
      numaCmd.addArg("numactl");

      Optional<Boolean> numaBindCpu = NativeProcessOptions.extractOptionValueAsBoolean(NumaProcessOptions.NUMA_BIND_CPU, processOptions);
      if (numaBindCpu.or(false)) {
        numaCmd.addArg("--cpunodebind=" + String.valueOf(numaCoreId.get()));
      }

      Optional<Boolean> numaBindMemory = NativeProcessOptions.extractOptionValueAsBoolean(NumaProcessOptions.NUMA_BIND_MEMORY, processOptions);
      if (numaBindMemory.or(false)) {
        numaCmd.addArg("--membind=" + String.valueOf(numaCoreId.get()));
      }

      numaCmd.addArg("--");
    }

    return numaCmd;
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
    CliUtil.extractUntilAvailable(handle.getInputStream(), anOutput, COMMAND_TIME_OUT);
    log.debug(anOutput.toString("UTF-8").trim());

    // Extract the error stream of the process
    anOutput.reset();
    IOUtil.extractAvailable(handle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error killing the process: " + anOutput.toString("UTF-8").trim());
    }
  }
}
