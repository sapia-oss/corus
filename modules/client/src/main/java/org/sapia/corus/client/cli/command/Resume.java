package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Resumes suspended processes.
 * 
 * @author Yanick Duchesne
 */
public class Resume extends CorusCliCommand {
  
  protected static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_DIST, OPT_VERSION, OPT_PROFILE, OPT_PROCESS_ID, OPT_CLUSTER
  );
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    ProcessCriteria.Builder builder = ProcessCriteria.builder();

    if (ctx.getCommandLine().containsOption(OPT_DIST.getName(), true)) {
      builder.distribution(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_DIST.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_VERSION.getName(), true)) {
      builder.version(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_VERSION.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROCESS_ID.getName(), true)) {
      builder.name(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_PROCESS_ID.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROCESS_ID.getName(), true)) {
      builder.pid(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_PROCESS_ID.getName(), true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROFILE.getName(), true)) {
      builder.profile(ctx.getCommandLine().assertOption(OPT_PROFILE.getName(), true).getValue());
    }

    displayProgress(ctx.getCorus().getProcessorFacade().resume(ProcessCriteria.builder().all(), getClusterInfo(ctx)), ctx);
  }
}
