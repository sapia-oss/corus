package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.services.processor.ProcessCriteria;


/**
 * @author Yanick Duchesne
 */
public class Resume extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    displayProgress(ctx.getCorus().getProcessorFacade().restart(ProcessCriteria.builder().all(), getClusterInfo(ctx)),
                    ctx);
  }
}
