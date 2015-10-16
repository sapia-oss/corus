package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdLine;

/**
 * Holds command-line data, in the context of process startup.
 * 
 * @author yduchesne
 *
 */
public class StarterResult {
  
  private StarterType starterType;
  private CmdLine     command;
  private boolean     interopEnabled;
  
  public StarterResult(StarterType starterType, CmdLine command, boolean interopEnabled) {
    this.starterType    = starterType;
    this.command        = command;
    this.interopEnabled = interopEnabled;
  }
  
  /**
   * @return <code>true</code> if interop should be enabled for the pending process, or <code>false</code>
   * if it should not (meaning that the process would not have a Corus interop agent running).
   */
  public boolean isInteropEnabled() {
    return interopEnabled;
  }
  
  /**
   * @return the {@link CmdLine} instance corresponding to the command to execute for 
   * starting a given process.
   */
  public CmdLine getCommand() {
    return command;
  }
  
  /**
   * @return this instance's {@link StarterType}, corresponding to the {@link Starter}. that generated
   * this instance's command line.
   */
  public StarterType getStarterType() {
    return starterType;
  }
  

}
