package org.sapia.corus.os;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
import org.sapia.console.ExecHandle;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.core.ModuleHelper;

/**
 * Implements the {@link OsModule} interface.
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = OsModule.class)
public class OsModuleImpl extends ModuleHelper implements OsModule {

  private static int BUFSZ = 1024;
  private static int COMMAND_TIME_OUT = 5000;

  // ///////////// Lifecycle

  @Override
  public void init() throws Exception {
  }

  @Override
  public void start() throws Exception {
  }

  @Override
  public void dispose() throws Exception {
  }

  // ///////////// OsModule interface

  public String getRoleName() {
    return OsModule.ROLE;
  }

  @Override
  public String executeProcess(LogCallback log, File rootDirectory, CmdLine commandLine) throws IOException {
    NativeProcess proc = NativeProcessFactory.newNativeProcess();
    return proc.exec(log, rootDirectory, commandLine);
  }

  @Override
  public void executeScript(LogCallback log, File rootDirectory, CmdLine commandLine) throws IOException {

    // Execute the command to start the process
    ExecHandle processHandle = commandLine.exec(rootDirectory, null);

    // Extract the output stream of the process
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
    CliUtils.extractUntilAvailable(processHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);

    log.debug(anOutput.toString("UTF-8").trim());

    // Extract the error stream of the process
    anOutput.reset();
    CliUtils.extractAvailable(processHandle.getErrStream(), anOutput);
    if (anOutput.size() > 0) {
      log.error("Error starting the process: " + anOutput.toString("UTF-8").trim());
    }

  }

  @Override
  public void killProcess(LogCallback log, KillSignal sig, String pid) throws IOException {
    NativeProcess proc = NativeProcessFactory.newNativeProcess();
    proc.kill(log, sig, pid);
  }

}
