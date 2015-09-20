package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.cli.CliContext;

/**
 * Displays environment variables.
 * 
 * @author yduchesne
 *
 */
public class Env extends CorusCliCommand {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    Map<String, String> env = System.getenv();
    for (String k : env.keySet()) {
      ctx.getConsole().println(k + " = " + env.get(k));
    }
  }
  
  protected void doInit(CliContext context) {
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return new ArrayList<>();
  }

}
