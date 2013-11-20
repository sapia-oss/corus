package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Resumes suspended processes.
 * 
 * @author Yanick Duchesne
 */
public class Resume extends CorusCliCommand {

  public static final String OPT_DIST = "d";
  public static final String OPT_VERSION = "v";
  public static final String OPT_PROFILE = "p";
  public static final String OPT_PROCESS = "n";
  public static final String OPT_PID = "i";

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    ProcessCriteria.Builder builder = ProcessCriteria.builder();

    if (ctx.getCommandLine().containsOption(OPT_DIST, true)) {
      builder.distribution(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_DIST, true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_VERSION, true)) {
      builder.version(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_VERSION, true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROCESS, true)) {
      builder.name(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_PROCESS, true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PID, true)) {
      builder.pid(ArgFactory.parse(ctx.getCommandLine().assertOption(OPT_PID, true).getValue()));
    }
    if (ctx.getCommandLine().containsOption(OPT_PROFILE, true)) {
      builder.profile(ctx.getCommandLine().assertOption(OPT_PROFILE, true).getValue());
    }

    displayProgress(ctx.getCorus().getProcessorFacade().resume(ProcessCriteria.builder().all(), getClusterInfo(ctx)), ctx);
  }
}
