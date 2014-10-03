package org.sapia.corus.client.cli.command.exec;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.Restart;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic for restarting processes by descriptors (
 * <code>restart -d * -v * -n * -p test</code>).
 * 
 * @author yduchesne
 * 
 */
public class RestartByProcessDescriptorsCommand extends RestartAndWaitCommandSupport {
  
  private static List<OptionDef> AVAIL_OPTIONS = new ArrayList<OptionDef>(RestartAndWaitCommandSupport.AVAIL_OPTIONS);
  static {
    AVAIL_OPTIONS.add(OPT_HARD_KILL);
  }

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

    CmdLine cmd = ctx.getCommandLine();
    String distName = cmd.assertOption(OPT_DIST.getName(), true).getValue();
    String version = cmd.assertOption(OPT_VERSION.getName(), true).getValue();
    String processName = cmd.containsOption(OPT_PROCESS_NAME.getName(), false) ? cmd.assertOption(OPT_PROCESS_NAME.getName(), true).getValue() : null;
    String profile = cmd.containsOption(OPT_PROFILE.getName(), false) ? cmd.assertOption(OPT_PROFILE.getName(), true).getValue() : null;

    ClusterInfo cluster = getClusterInfo(ctx);

    ProcessCriteria criteria = ProcessCriteria.builder().name(processName).distribution(distName).version(version).profile(profile).build();

    Option wait = getWaitOption(ctx);
    KillPreferences prefs = KillPreferences.newInstance().setHard(isHardKillOption(ctx));
    if (wait != null) {
      ctx.getConsole().println("Waiting for process restart, please stand by...");
      doRestartAndWait(ctx, cluster, criteria, prefs, wait.getValue() == null ? Restart.DEFAULT_RESTART_WAIT_TIME_SECONDS : wait.asInt());
    } else {
      ctx.getConsole().println("Triggering process restart...");
      ctx.getCorus().getProcessorFacade().restart(criteria, prefs, getClusterInfo(ctx));
    }
  }

}
