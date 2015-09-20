package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.ubik.util.Collects;

/**
 * Forces a repo pull.
 * 
 * @author yduchesne
 */
public class Pull extends CorusCliCommand {

  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    ctx.getCorus().getRepoFacade().pull(getClusterInfo(ctx));
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_CLUSTER);
  }

}
