package org.sapia.corus.cloud.platform.cli;

import org.sapia.console.CmdLine;

/**
 * 
 * @author yduchesne
 *
 */
public interface CliModule {
  
  public enum StatusCode {
    SUCCESS, FAILURE;
  }

  /**
   * @return the identifier of the cloud provider corresponding to this instance.
   */
  public String getProvider();
  
  
  /**
   * @return the CLI command name used to invoke this module, in the context of its provider.
   */
  public String getCommandName();
  
  /**
   * @param context the {@link CliModuleContext} to use in the context of a user interaction.
   * @param the {@link CmdLine} instance corresponding to the initial command entered by the end-user that
   * triggered invocation of this module.
   */
  public StatusCode interact(CliModuleContext context, CmdLine initialCommand);
  
  /**
   * @param context the {@link CliModuleContext} to use in the context of a user interaction.
   */
  public void displayHelp(CliModuleContext context);
}
