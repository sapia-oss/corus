package org.sapia.corus.client.cli.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.exec.ExecProcessByDescriptors;
import org.sapia.corus.client.cli.command.exec.ExecProcessesByConfig;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;

/**
 * Executes processes.
 * 
 * @author Yanick Duchesne
 */
public class Exec extends AbstractExecCommand {

  public static final OptionDef OPT_EXEC_CONFIG = new OptionDef("e", true);
  public static final OptionDef OPT_SCRIPT      = new OptionDef("s", true);
  public static final OptionDef OPT_REV         = new OptionDef("rev", true);
 
  public static final String ARG_ENABLE    = "enable";
  public static final String ARG_DISABLE   = "disable";
  public static final String ARG_ARCHIVE   = "archive";
  public static final String ARG_UNARCHIVE = "unarchive";
  
  private static final List<OptionDef> ADDITIONAL_OPTIONS = new ArrayList<OptionDef>(AVAIL_OPTIONS);
  
  static {
    ADDITIONAL_OPTIONS.add(OPT_EXEC_CONFIG);
    ADDITIONAL_OPTIONS.add(OPT_SCRIPT);
    ADDITIONAL_OPTIONS.add(OPT_REV);
  }
  
  @Override
  protected void doInit(CliContext context) {
  }
 
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    if (ctx.getCommandLine().isNextArg()) {
      String operation  = ctx.getCommandLine().assertNextArg(new String[] {ARG_ENABLE, ARG_DISABLE, ARG_ARCHIVE, ARG_UNARCHIVE}).getName();
      if (operation.equalsIgnoreCase(ARG_ENABLE)) {
        String configName = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG.getName(), true).getValue();
        ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(configName)).build();
        ctx.getCorus().getProcessorFacade().setExecConfigEnabled(crit, true, getClusterInfo(ctx));
      } else if (operation.equalsIgnoreCase(ARG_DISABLE)) {
        String configName = ctx.getCommandLine().assertOption(OPT_EXEC_CONFIG.getName(), true).getValue();
        ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(configName)).build();
        ctx.getCorus().getProcessorFacade().setExecConfigEnabled(crit, false, getClusterInfo(ctx));
      } else if (operation.equalsIgnoreCase(ARG_ARCHIVE)) {
        String revId = ctx.getCommandLine().assertOption(OPT_REV.getName(), true).getValue();
        ctx.getCorus().getProcessorFacade().archiveExecConfigs(
            RevId.valueOf(revId), getClusterInfo(ctx)
        );
      } else if (operation.equalsIgnoreCase(ARG_UNARCHIVE)) {
        String revId = ctx.getCommandLine().assertOption(OPT_REV.getName(), true).getValue();
        ctx.getCorus().getProcessorFacade().unarchiveExecConfigs(
            RevId.valueOf(revId), getClusterInfo(ctx)
        );
      } else {
        throw new InputException("Expected enable | disable | archive | unarchive");
      }
    } else if (ctx.getCommandLine().containsOption(OPT_EXEC_CONFIG.getName(), true)) {
      new ExecProcessesByConfig().execute((Context) ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_SCRIPT.getName(), true)) {
      doExecuteScript(ctx);
    } else {
      new ExecProcessByDescriptors().execute((Context) ctx);
    }
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return ADDITIONAL_OPTIONS;
  }

  private void doExecuteScript(CliContext ctx) throws AbortException, InputException {
    ClusterInfo cluster = getClusterInfo(ctx);
    String alias = ctx.getCommandLine().assertOption(OPT_SCRIPT.getName(), true).getValue();
    try {
      displayProgress(ctx.getCorus().getScriptManagementFacade().execScript(alias, cluster), ctx);
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be executed: " + alias, e);
    } catch (ScriptNotFoundException e) {
      ctx.createAndAddErrorFor(this, "Shell script could not be found for alias: " + alias, e);
    }
  }

}
