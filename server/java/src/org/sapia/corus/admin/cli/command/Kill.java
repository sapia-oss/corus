package org.sapia.corus.admin.cli.command;

import java.util.Iterator;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LogicException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Kill extends CorusCliCommand {
  protected boolean _suspend;

  protected Kill(boolean suspend) {
    _suspend = suspend;
  }

  public Kill() {
  }

  /**
   * @see CorusCliCommand#doExecute(CliContext)
   */
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    String  profile = null;
    String  vmName  = null;
    String  vmId    = null;
    String  osPid   = null;

    CmdLine cmd = ctx.getCommandLine();

    // Kill by VM IDENTIDER
    if (cmd.containsOption(CorusCliCommand.VM_ID_OPT, true)) {
      vmId = cmd.assertOption(CorusCliCommand.VM_ID_OPT, true).getValue();
      killProcessByVmId(ctx, vmId);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          killProcessByVmId(ctx, argument.getName());
        } else {
          cmd.next();
        }
      }

    // Kill by OS PROCESS ID
    } else if (cmd.containsOption(CorusCliCommand.OS_PID_OPT, true)) {
      osPid = cmd.assertOption(CorusCliCommand.OS_PID_OPT, true).getValue();
      killProcessByOsPid(ctx, osPid);
      
      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          killProcessByOsPid(ctx, argument.getName());
        } else {
          cmd.next();
        }
      }
      
    // KILL BY DISTRIBUTION ATTIRBUTES
    } else {
      
      dist = cmd.assertOption(CorusCliCommand.DIST_OPT, true).getValue();
  
      version = cmd.assertOption(CorusCliCommand.VERSION_OPT, true).getValue();
  
      if(cmd.containsOption(CorusCliCommand.PROFILE_OPT, true)){
        profile = cmd.assertOption(CorusCliCommand.PROFILE_OPT, true).getValue();
      }
      
      if (cmd.containsOption(CorusCliCommand.VM_NAME_OPT, true)) {
        vmName = cmd.assertOption(CorusCliCommand.VM_NAME_OPT, true).getValue();
      }
  
      ClusterInfo cluster = getClusterInfo(ctx);
  
      if (vmName != null) {
        if (_suspend) {
          ctx.getCorus().suspend(dist, version, profile, vmName, cluster);
        } else {
          ctx.getCorus().kill(dist, version, profile, vmName, cluster);
        }
      } else {
        if (_suspend) {
          ctx.getCorus().suspend(dist, version, profile, cluster);
        } else {
          ctx.getCorus().kill(dist, version, profile, cluster);
        }
      }
  
      ctx.getConsole().println("Proceeding to kill...");
    }
  }
  
  /**
   * 
   * @param ctx
   * @param vmId
   * @throws InputException
   */
  protected void killProcessByVmId(CliContext ctx, String vmId) throws InputException {
    Process processToKill = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToKill == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToKill == null; ) {
        Process process = (Process) it.next();
        if (process.getProcessID().equals(vmId)) {
          processToKill = process;
        }
      }
    }
    
    if (processToKill != null) {
      killProcess(ctx, processToKill);
    } else {
      ctx.getConsole().println("ERROR: Could not kill process, no active process found for the VM id " + vmId);
    }
  }
  
  /**
   * 
   * @param ctx
   * @param osPid
   * @return
   * @throws InputException 
   */
  protected void killProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    Process processToKill = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToKill == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToKill == null; ) {
        Process aProcess = (Process) it.next();
        if (aProcess.getOsPid() != null && aProcess.getOsPid().equals(osPid)) {
          String vmId = aProcess.getProcessID();
          processToKill = aProcess;
          ctx.getConsole().println("Found VM " + vmId + " associated to OS pid " + osPid);
        }
      }
    }

    if (processToKill != null) {
      killProcess(ctx, processToKill);
    } else {
      ctx.getConsole().println("ERROR: Could not kill process, no active process found for OS pid " + osPid);
    }
  }
  
  /**
   * 
   * @param ctx
   * @param aProcess
   * @throws InputException
   */
  protected void killProcess(CliContext ctx, Process aProcess) throws InputException {
    if (_suspend) {
      ctx.getCorus().suspend(aProcess.getProcessID());
      ctx.getConsole().println("Suspending VM " + aProcess.getProcessID() + "...");
    } else {
      try {
        ctx.getCorus().kill(aProcess.getProcessID());
      } catch (LogicException e) {
        throw new InputException(e.getMessage());
      }
  
      ctx.getConsole().println("Proceeding to kill of VM " + aProcess.getProcessID() + "...");
    }
  }
}
