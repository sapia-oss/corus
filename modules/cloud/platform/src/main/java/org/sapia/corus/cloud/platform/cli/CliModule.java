package org.sapia.corus.cloud.platform.cli;

import org.sapia.console.CmdLine;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;

/**
 * 
 * @author yduchesne
 *
 */
public interface CliModule {
  
  /**
   * Holds constants corresponding to the typically success/failure status codes under Linux/Unix.
   * @author yduchesne
   *
   */
  public enum StatusCode {
    
    SUCCESS(0), FAILURE(1);
    
    private int value;
    
    private StatusCode(int value) {
      this.value = value;
    }
    
    public int value() {
      return this.value;
    }
    
  }
  
  /**
   * @param context the {@link CliModuleContext} to use in the context of a user interaction.
   * @param the {@link CmdLine} instance corresponding to the initial command entered by the end-user that
   * triggered invocation of this module.
   */
  public WorkflowResult interact(CliModuleContext context, CmdLine initialCommand);
  
  /**
   * @param context the {@link CliModuleContext} to use in the context of a user interaction.
   */
  public void displayHelp(CliModuleContext context);
}
