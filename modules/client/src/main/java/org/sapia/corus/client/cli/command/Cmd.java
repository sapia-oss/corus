package org.sapia.corus.client.cli.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.sapia.console.AbortException;
import org.sapia.console.ExecHandle;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.CliUtils;

/**
 * Invokes an OS command.
 * 
 * @author yduchesne
 *
 */
public class Cmd extends CorusCliCommand {
  
  private static int BUFSZ = 1024;
  private static int COMMAND_TIME_OUT = 5000;

  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    try {
      ExecHandle handle = ctx.getCommandLine().exec();
      
      // Extract the output stream of the process
      ByteArrayOutputStream anOutput = new ByteArrayOutputStream(BUFSZ);
      CliUtils.extractUntilAvailable(handle.getInputStream(), anOutput, COMMAND_TIME_OUT);
      
      ctx.getConsole().println(anOutput.toString("UTF-8").trim());
      
      // Extract the error stream of the process
      anOutput.reset();
      CliUtils.extractAvailable(handle.getErrStream(), anOutput);
      if (anOutput.size() > 0) {
        ctx.getConsole().println(anOutput.toString("UTF-8").trim());
        throw new AbortException("Aborting on process error");
      }
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, e);
    }
  }
}
