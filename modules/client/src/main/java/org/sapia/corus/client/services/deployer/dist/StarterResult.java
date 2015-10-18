package org.sapia.corus.client.services.deployer.dist;

import java.util.HashMap;
import java.util.Map;

import org.sapia.console.CmdLine;
import org.sapia.ubik.util.Assertions;

/**
 * Holds command-line data, in the context of process startup.
 * 
 * @author yduchesne
 *
 */
public class StarterResult {
  
  private StarterType         starterType;
  private CmdLine             command;
  private boolean             interopEnabled;
  private Map<String, Object> attachments = new HashMap<String, Object>();
  
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
  
  /**
   * @param name the name of the given attachment.
   * @param attachment an arbitrary {@link Object} to attach to this instance.
   */
  public void set(String name, Object attachment) {
    attachments.put(name, attachment);
  }
  
  /**
   * @param name the name of the attachment to return.
   * @param type the type to which to cast the given attachment.
   * @return the attachment corresponding to the given name, or <code>null</code> if
   * no such attachment exists.
   */
  public <T> T get(String name, Class<T> type) {
    return type.cast(attachments.get(name));
  }

  /**
   * @param name the name of the attachment to return.
   * @param type the type to which to cast the given attachment.
   * @return the attachment corresponding to the given name.
   * @throws IllegalStateException if no such attachment exists. 
   */
  public <T> T getNotNull(String name, Class<T> type) throws IllegalStateException {
    Object att = attachments.get(name);
    Assertions.illegalState(att == null, "No attachment found for: %s", name);
    return type.cast(att);
  }
  
}
