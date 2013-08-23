package org.sapia.corus.client.cli.command.exec;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.cli.command.Exec;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic for executing processes by descriptors (<code>exec -d * -v * -n * -p myProfile</code>).
 * 
 * @author yduchesne
 *
 */
public class ExecProcessByDescriptors extends AbstractExecCommand {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    String  dist      = null;
    String  version   = null;
    String  profile   = null;
    String  vmName    = null;
    int     instances = 1;
    CmdLine cmd       = ctx.getCommandLine();

    dist = cmd.assertOption(DIST_OPT, true).getValue();

    version = cmd.assertOption(VERSION_OPT, true).getValue();

    profile = cmd.assertOption(PROFILE_OPT, true).getValue();

    if (cmd.containsOption(VM_NAME_OPT, true)) {
      vmName = cmd.assertOption(VM_NAME_OPT, true).getValue();
    }

    if (cmd.containsOption(VM_INSTANCES, true)) {
      instances = cmd.assertOption(VM_INSTANCES, true).asInt();
    }

    ClusterInfo cluster = getClusterInfo(ctx);
    ProcessCriteria criteria = ProcessCriteria.builder()
      .name(vmName)
      .distribution(dist)
      .version(version)
      .profile(profile)
      .build();
    
    try {
      displayProgress(
              ctx.getCorus().getProcessorFacade().exec(criteria, instances, cluster),
              ctx
      );
      
      Option waitOpt = getOpt(ctx, OPT_WAIT);
      if (waitOpt != null) {
        waitForProcessStartup(
            ctx, 
            criteria, 
            instances, 
            waitOpt.getValue() == null ? Exec.DEFAULT_EXEC_WAIT_TIME_SECONDS : waitOpt.asInt(), 
            cluster);
        ctx.getConsole().println("Process startup completed on all nodes");
      }
      
    } catch(TooManyProcessInstanceException e){
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }    
    
  }

}
