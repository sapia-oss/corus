package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.ubik.util.Collects;

/**
 * Triggers execution of the <code>rollback.corus</code> script.
 * 
 * @author yduchesne
 *
 */
public class Rollback extends CorusCliCommand {
  
  private static List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(OPT_DIST, OPT_VERSION, OPT_CLUSTER);
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    String name    = ctx.getCommandLine().assertOption(OPT_DIST.getName(), true).getValue();
    String version = ctx.getCommandLine().assertOption(OPT_VERSION.getName(), true).getValue();
    
    try {
      displayProgress(ctx.getCorus().getDeployerFacade().rollbackDistribution(name, version, getClusterInfo(ctx)), ctx);
    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());    
    }
  }
  

}
