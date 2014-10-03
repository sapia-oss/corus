package org.sapia.corus.client.cli.command.exec;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic for executing processes by descriptors (
 * <code>exec -d * -v * -n * -p myProfile</code>).
 * 
 * @author yduchesne
 * 
 */
public class ExecProcessByDescriptors extends AbstractExecCommand {
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void validate(CmdLine cmdLine) throws InputException {
    super.validate(cmdLine);
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    String dist = null;
    String version = null;
    String profile = null;
    String vmName = null;
    int instances = 1;
    CmdLine cmd = ctx.getCommandLine();

    dist = cmd.assertOption(OPT_DIST.getName(), true).getValue();

    version = cmd.assertOption(OPT_VERSION.getName(), true).getValue();

    profile = cmd.assertOption(OPT_PROFILE.getName(), true).getValue();

    if (cmd.containsOption(OPT_PROCESS_NAME.getName(), true)) {
      vmName = cmd.assertOption(OPT_PROCESS_NAME.getName(), true).getValue();
    }

    if (cmd.containsOption(OPT_PROCESS_INSTANCES.getName(), true)) {
      instances = cmd.assertOption(OPT_PROCESS_INSTANCES.getName(), true).asInt();
    }

    ClusterInfo cluster = getClusterInfo(ctx);
    ProcessCriteria criteria = ProcessCriteria.builder().name(vmName).distribution(dist).version(version).profile(profile).build();

    try {
      displayProgress(ctx.getCorus().getProcessorFacade().exec(criteria, instances, cluster), ctx);

      Option waitOpt = getOpt(ctx, OPT_WAIT.getName());
      if (waitOpt != null) {
        waitForProcessStartup(ctx, criteria, instances, waitOpt.getValue() == null ? DEFAULT_EXEC_WAIT_TIME_SECONDS : waitOpt.asInt(), cluster);
        ctx.getConsole().println("Process startup completed on all nodes");
      }

    } catch (TooManyProcessInstanceException e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }

  }

}
