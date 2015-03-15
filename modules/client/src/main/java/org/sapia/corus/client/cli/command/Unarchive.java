package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.ubik.util.Collects;

/**
 * Unarchives process properties, tags, exec configs, port ranges.
 * 
 * @author yduchesne
 *
 */
public class Unarchive extends CorusCliCommand {
  
  private static final OptionDef OPT_REV = new OptionDef("rev", true);
  
  private static final List<OptionDef> AVAILABLE_OPTIONS = Collects.arrayToList(OPT_REV, OPT_CLUSTER);
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAILABLE_OPTIONS;
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    RevId  rev = RevId.valueOf(ctx.getCommandLine().getOptNotNull(OPT_REV.getName()).getValueNotNull());
    ClusterInfo cluster = getClusterInfo(ctx);
    
    ctx.getConsole().println("Unarchiving...");
    ctx.getConsole().println("  => Process properties");
    ctx.getCorus().getConfigFacade().unarchiveProcessProperties(rev, cluster);
    ctx.getConsole().println("  => Tags");
    ctx.getCorus().getConfigFacade().unarchiveTags(rev, cluster);      
    ctx.getConsole().println("  => Port ranges");
    ctx.getCorus().getPortManagementFacade().unarchive(rev, cluster);
    ctx.getConsole().println("  => Execution configurations");
    ctx.getCorus().getProcessorFacade().unarchiveExecConfigs(rev, cluster);      
    ctx.getConsole().println("Unarchiving completed");
    
  }
  
}
