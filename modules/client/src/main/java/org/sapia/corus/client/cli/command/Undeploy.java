package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Performs undeployment.
 * 
 * @author Yanick Duchesne
 */
public class Undeploy extends CorusCliCommand {

  private static final OptionDef OPT_EXEC_CONFIG = new OptionDef("e", true);
  private static final OptionDef OPT_SCRIPT      = new OptionDef("s", true);
  private static final OptionDef OPT_FILE        = new OptionDef("f", true);
  private static final OptionDef OPT_BACKUP      = new OptionDef("backup", true);
  private static final OptionDef OPT_REV         = new OptionDef("rev", true);

  
  private static List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_EXEC_CONFIG, OPT_SCRIPT, OPT_FILE, OPT_DIST, OPT_VERSION, OPT_BACKUP, OPT_REV, OPT_CLUSTER
  );
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    if (ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG.getName(), false)) {
      doUndeployExecConfig(ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_SCRIPT.getName(), false)) {
      doUndeployScript(ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_FILE.getName(), false)) {
      doUndeployFile(ctx);
    } else {
      doUndeployDist(ctx);
    }
  }

  private void doUndeployExecConfig(CliContext ctx) throws AbortException, InputException {
    String name = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG.getName(), true).getValue();
    int backup  = 0;
    if (ctx.getCommandLine().containsOption(OPT_BACKUP.getName(), false)) {
      backup = ctx.getCommandLine().assertOption(OPT_BACKUP.getName(), true).asInt();
    }
    ExecConfigCriteria crit = ExecConfigCriteria.builder().backup(backup).name(ArgMatchers.parse(name)).build();
    ctx.getCorus().getProcessorFacade().undeployExecConfig(crit, getClusterInfo(ctx));
  }

  private void doUndeployDist(CliContext ctx) throws AbortException, InputException {
    try {
      String dist    = null;
      String version = null;
      int    backup  = 0;
      CmdLine cmd = ctx.getCommandLine();

      if (cmd.hasNext() && cmd.isNextArg()) {
        cmd.assertNextArg(new String[] { ARG_ALL });
        dist = WILD_CARD;
        version = WILD_CARD;
      } else {
        dist = cmd.assertOption(OPT_DIST.getName(), true).getValue();
        version = cmd.assertOption(OPT_VERSION.getName(), true).getValue();
      }
      
      if (ctx.getCommandLine().containsOption(OPT_BACKUP.getName(), false)) {
        backup = ctx.getCommandLine().assertOption(OPT_BACKUP.getName(), true).asInt();
      }
    
      UndeployPreferences prefs = UndeployPreferences.newInstance();
      if (ctx.getCommandLine().containsOption(OPT_REV.getName(), false)) {
        prefs.revId(RevId.valueOf(ctx.getCommandLine().getOptNotNull(OPT_REV.getName()).getValueNotNull()));
      }

      super.displayProgress(
          ctx.getCorus().getDeployerFacade().undeployDistribution(
               DistributionCriteria.builder()
                 .name(dist).version(version)
                 .backup(backup).build(), 
               prefs,
               getClusterInfo(ctx)
          ), 
          ctx
      );

    } catch (RunningProcessesException e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  private void doUndeployScript(CliContext ctx) throws InputException {
    String alias = ctx.getCommandLine().assertOption(OPT_SCRIPT.getName(), true).getValue();
    ProgressQueue progress = ctx.getCorus().getScriptManagementFacade()
        .removeScripts(ShellScriptCriteria.newInstance().setAlias(ArgMatchers.parse(alias)), getClusterInfo(ctx));
    displayProgress(progress, ctx);
  }

  private void doUndeployFile(CliContext ctx) throws InputException {
    String name = ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue();
    ProgressQueue progress = ctx.getCorus().getFileManagementFacade()
        .deleteFiles(FileCriteria.newInstance().setName(ArgMatchers.parse(name)), getClusterInfo(ctx));
    displayProgress(progress, ctx);
  }

}
