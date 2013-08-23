package org.sapia.corus.client.cli.command.exec;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.Restart;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic for restarting processes by descriptors (<code>restart -d * -v * -n * -p test</code>).
 * 
 * @author yduchesne
 *
 */
public class RestartByProcessDescriptorsCommand extends RestartAndWaitCommandSupport {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
   
    CmdLine cmd         = ctx.getCommandLine();
    String distName     = cmd.assertOption(DIST_OPT, true).getValue();
    String version      = cmd.assertOption(VERSION_OPT, true).getValue();
    String processName  = cmd.containsOption(VM_NAME_OPT, false) ? cmd.assertOption(VM_NAME_OPT, true).getValue() : null;
    String profile      = cmd.containsOption(PROFILE_OPT, false) ? cmd.assertOption(PROFILE_OPT, true).getValue() : null;
    
    ClusterInfo cluster = getClusterInfo(ctx);

    ProcessCriteria criteria = ProcessCriteria.builder()
      .name(processName)
      .distribution(distName)
      .version(version)
      .profile(profile)
      .build();
    
    Option wait = getWaitOption(ctx);
    if (wait != null) {
      ctx.getConsole().println("Waiting for process restart, please stand by...");      
      doRestartAndWait(
          ctx, 
          cluster, 
          criteria, 
          wait.getValue() == null ? Restart.DEFAULT_RESTART_WAIT_TIME_SECONDS : wait.asInt());
    } else {
      ctx.getConsole().println("Triggering process restart...");      
      ctx.getCorus().getProcessorFacade().restart(criteria, getClusterInfo(ctx));      
    }
  }

}
