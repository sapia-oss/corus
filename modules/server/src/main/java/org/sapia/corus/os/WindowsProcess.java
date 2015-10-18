package org.sapia.corus.os;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarPermissionDeniedException;
import org.javasimon.callback.logging.LoggingCallback;
import org.sapia.console.CmdLine;
import org.sapia.console.ExecHandle;
import org.sapia.console.Option;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.sigar.SigarSupplier;
import org.sapia.corus.util.IOUtil;

/**
 * Windows implementation of the {@link NativeProcess} interface.
 *
 * @author Yanick Duchesne
 *
 */
public class WindowsProcess implements NativeProcess {
  
  /**
   * Abstracts how the process is killed.
   * 
   * @author yduchesne
   *
   */
  public interface KillProcessFunction {
    
    /**
     * @param log the {@link LoggingCallback} instance to log to.
     * @param pid the OS pid of the process to kill.
     */
    public void call(LogCallback log, String pid) throws IOException;
    
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Kills using the <code>pv.exe</code> executable
   * 
   * @author yduchesne
   *
   */
  public class KillWithPvFunction implements KillProcessFunction {

    @Override
    public void call(LogCallback log, String pid) throws IOException {
  
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
  
  // --------------------------------------------------------------------------
  
  /**
   * Kills using the <code>taskkill</code> executable
   * 
   * @author yduchesne
   *
   */
  public class KillWithTaskKillFunction implements KillProcessFunction {

    @Override
    public void call(LogCallback log, String pid) throws IOException {
  
      // Generate the kill command
      CmdLine aKillCommand = CmdLine.parse(String.format("cmd /c taskkill /PID %s", pid));
  
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
  
  // --------------------------------------------------------------------------
  
  /**
   * Kills using <code>SIGAR</code>.
   * 
   * @author yduchesne
   *
   */
  public class KillWithSigarFunction implements KillProcessFunction {
    
    @Override
    public void call(LogCallback log, String pid) throws IOException {
      try {
        log.debug("--> Killing with SIGAR: " + pid);

        ((Sigar) SigarSupplier.get()).kill(Long.parseLong(pid), KILL_SIG);
      } catch (SigarException e) {
        log.debug("Error caught trying to kill process: " + e.getMessage());
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Chains a series {@link KillProcessFunction}.
   * 
   * @author yduchesne
   *
   */
  public class CompositeKillFunction implements KillProcessFunction {
    
    private List<KillProcessFunction> funcs = new ArrayList<WindowsProcess.KillProcessFunction>();
    
    @Override
    public void call(LogCallback log, String pid) throws IOException {
      for (KillProcessFunction f : funcs) {
        f.call(log, pid);
        if (SigarSupplier.isSet()) {
          try {
            ProcExe exe = SigarSupplier.get().getProcExe(pid);
            if (exe == null) {
              log.debug(String.format("Process %s can no more be found: assuming it was successfully killed", pid));
              break;
            } else {
              log.debug(String.format("Process %s is still up. Diagnostics:", pid));
              log.debug("  Name:              " + exe.getName());
              log.debug("  Process directory: " + exe.getCwd());
            }
          } catch (SigarException e) {
            log.debug(String.format("Got SIGAR exception, process %s unavailable (probably been killed successfully): %s)", pid, e.getMessage()));
            break;
          }
        }
      }
    }
    
  }
  
  // ==========================================================================

  private static final long PAUSE_AFTER_START = 1000;
  private static final int  BUFSZ             = 1024;
  private static final int  COMMAND_TIME_OUT  = 5000;
  private static final int  KILL_SIG          = 9;

  /**
   * Utility method that returns a new CmdLine object with the path to the
   * process viewer tool.
   *
   * @return A CmdLine containing the path to the process viewer tool.
   * @throws IOException
   *           If the process viewer tool is not found
   */
  private synchronized CmdLine createPVCmdLine() throws IOException {
    // Generate the command line to the process viewer tool
    File winPath = FilePath.newInstance()
        .addDir(System.getProperty("corus.home"))
        .addDir("bin")
        .addDir("win")
        .addDir("pv.exe").createFile();
    
    File win32Path = FilePath.newInstance()
        .addDir(System.getProperty("corus.home"))
        .addDir("bin")
        .addDir("win32")
        .addDir("pv.exe").createFile();
    
    File win64Path = FilePath.newInstance()
        .addDir(System.getProperty("corus.home"))
        .addDir("bin")
        .addDir("win64")
        .addDir("pv.exe").createFile();      
  
    // Validate the presence and accessibility of the process viewer tool
    if (winPath.exists()) {
      return new CmdLine().addArg(winPath.getAbsolutePath());
    } if (win32Path.exists()) {
      return new CmdLine().addArg(win32Path.getAbsolutePath());
    } if (win64Path.exists()) {
      return new CmdLine().addArg(win64Path.getAbsolutePath());
    } else {
      throw new IOException(String.format("Unable to find process viewer executable at either %s, %s or %s", 
          winPath.getAbsolutePath(), win32Path.getAbsolutePath(), win64Path.getAbsolutePath()));
    }      
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
  public String exec(LogCallback log, File baseDir, CmdLine cmd) throws IOException {
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
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);

    try {
      // Extract the output stream of the process
      CliUtils.extractUntilAvailable(vmHandle.getInputStream(), anOutput, COMMAND_TIME_OUT);
      log.debug(anOutput.toString("UTF-8"));
  
      // Extract the error stream of the process
      anOutput.reset();
      IOUtil.extractAvailable(vmHandle.getErrStream(), anOutput);
      if (anOutput.size() > 0) {
        log.error("Error starting the process: " + anOutput.toString("UTF-8"));
      }
    } catch (IOException e) {
      if (SigarSupplier.isSet()) {
        log.debug("Error occurred starting process, checking if PID exists. Error was: " + e.getMessage());
        try {
          Thread.sleep(PAUSE_AFTER_START);
        } catch (InterruptedException e2) {
          throw new IOException("Thread was interrupted while pausing after process exec", e);
        }
        
        String startedPid = extractPidUsingSigar(log, baseDir);
        if (startedPid != null) {
          log.debug("Got PID despite process output having closed prematurely. PID is: " + startedPid);
          return startedPid;
        } else {
          throw e;
        }
      } else {
        throw e;
      }
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


  @Override
  public void kill(LogCallback log, KillSignal sig, String pid) throws IOException {
    if (SigarSupplier.isSet() && SigarSupplier.get() instanceof Sigar) {
      try {
        log.debug(String.format("Killing process %s with SIGAR", pid));
        
        CompositeKillFunction func = new CompositeKillFunction();
        func.funcs.add(new KillWithSigarFunction());
        func.funcs.add(new KillWithTaskKillFunction());
        func.call(log, pid);
      } catch (Exception e) {
        log.debug(String.format("Error caught trying to kill process with SIGAR - falling back to default mechanism: %s", e.getMessage()));
        killWithPv(log, pid);
      }
    } else {
      killWithPv(log, pid);
    }
  }
  
  private String extractPidUsingSigar(LogCallback log, File baseDir)
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
    } catch (UnsatisfiedLinkError e) {
      throw new IOException("Could not load Sigar native lib", e);
    }
  }
  
  private void killWithPv(LogCallback log, String pid) throws IOException {
    KillWithPvFunction func = new KillWithPvFunction();
    func.call(log, pid); 
  }
  
  private String extractPidUsingPV(LogCallback log, CmdLine cmd, ByteArrayOutputStream anOutput, File baseDir)
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
}
