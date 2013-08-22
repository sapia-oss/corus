package org.sapia.corus.client.cli.command;

import java.util.concurrent.TimeUnit;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * This command forces a pause for a given amount of seconds.
 * 
 * @author yduchesne
 *
 */
public class Pause extends CorusCliCommand {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    int seconds = Integer.parseInt(ctx.getCommandLine().assertNextArg().getName());
    try {
      Thread.sleep(TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted while pausing");
    }
  }
}
