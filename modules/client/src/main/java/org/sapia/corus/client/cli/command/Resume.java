package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Resumes suspended processes.
 * 
 * @author Yanick Duchesne
 */
public class Resume extends CorusCliCommand {
  
  protected static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_DIST, OPT_VERSION, OPT_PROCESS_NAME, OPT_PROFILE, OPT_PROCESS_ID, OPT_CLUSTER
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

    ProcessCriteria.Builder builder = ProcessCriteria.builder();

    if (ctx.getCommandLine().containsOption(OPT_DIST.getName(), true)) {
      builder.distribution(ArgMatchers.parse(ctx.getCommandLine().assertOption(OPT_DIST.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_VERSION.getName(), true)) {
      builder.version(ArgMatchers.parse(ctx.getCommandLine().assertOption(OPT_VERSION.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROCESS_NAME.getName(), true)) {
      builder.name(ArgMatchers.parse(ctx.getCommandLine().assertOption(OPT_PROCESS_NAME.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROCESS_ID.getName(), true)) {
      builder.pid(ArgMatchers.parse(ctx.getCommandLine().assertOption(OPT_PROCESS_ID.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROFILE.getName(), true)) {
      builder.profile(ctx.getCommandLine().assertOption(OPT_PROFILE.getName(), true).getValue());
    }

    displayProgress(ctx.getCorus().getProcessorFacade().resume(ProcessCriteria.builder().all(), getClusterInfo(ctx)), ctx);
  }
}
