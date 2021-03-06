package org.sapia.corus.client;

/**
 * Utility class for performing logging on the client-side. Logs to stdout.
 * 
 * @author yduchesne
 *
 */
public class ClientDebug {
  
  public interface ClientDebugOutput {
    
    public void println(String line);
    
  }
  
  // ==========================================================================
  
  private static final boolean ENABLED = System.getProperty("corus.client.debug.enabled", "false").equalsIgnoreCase("true");
  
  private String            owner;
  
  private static ClientDebugOutput OUTPUT = new ClientDebugOutput() {
    @Override
    public void println(String line) {
      System.out.println(line);
    }
  };
  
  private ClientDebug(String owner) {
    this.owner = owner;
  }
  
  /**
   * @param output {@link ClientDebugOutput} to redirect output to.
   */
  public static void setOutput(ClientDebugOutput output) {
    OUTPUT = output;
  }
  
  /**
   * @param owner the name of the owner to which the returned instance is associated.
   * @return a new {@link ClientDebug} instance.
   */
  public static ClientDebug get(String owner) {
    return new ClientDebug(owner);
  }

  /**
   * @param owner the class of the owner to which the returned instance is associated.
   * @return a new {@link ClientDebug} instance.
   */
  public static ClientDebug get(Class<?> owner) {
    return new ClientDebug(owner.getSimpleName());
  }
  
  /**
   * @param msg a debug message.
   * @param args the message's arguments.
   */
  public void trace(String msg, Object...args) {
    if (ENABLED) {
      OUTPUT.println("[" + owner + "] " + String.format(msg, args));
    }
  }
  
  /**
   * @return <code>true</code> if tracing is enabled.
   */
  public boolean enabled() {
    return ENABLED;
  }
  
}
