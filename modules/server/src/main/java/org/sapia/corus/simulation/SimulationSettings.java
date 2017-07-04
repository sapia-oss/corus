package org.sapia.corus.simulation;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.log.Logger;
import org.sapia.ubik.util.Conf;

/**
 * Provides access to simulation configuration properties used on the server-side.
 * 
 * @author yduchesne
 *
 */
public class SimulationSettings {
  
  private static Conf conf = Conf.getSystemProperties();
  
  private SimulationSettings() {
    
  }
  
  /**
   * Initializes this instance.
   * 
   * @param toUse the {@link Conf} to use.
   */
  public static void initialize(Conf toUse) {
    conf = toUse;
  }
  
  /**
   * @return <code>true</code> if deployment interruption is enabled - <code>false</code> otherwise.
   */
  public static boolean isDeploymentInterruptionEnabled() {
    return conf.getBooleanProperty("corus.server.simulation.deployment.interrupted", false);
  }
  
  /**
   * Dumps the simulation settings to the given {@link PrintWriter}.
   * 
   * @param pw the {@link PrintWriter} to write to.
   */
  public static void dump(PrintWriter pw) {
    pw.println("***** Simulation settings: *****");
    pw.println(">> Deployment interruption enabled: " + isDeploymentInterruptionEnabled());
    pw.flush();
  }
  
  /**
   * Dumps the simulation settings to the given {@link PrintStream}.
   * 
   * @param pw the {@link PrintStream} to write to.
   */
  public static void dump(PrintStream ps) {
    dump(new PrintWriter(ps));
  }

  /**
   * Dumps the simulation settings to the given {@link Logger}.
   * 
   * @param logger the {@link Logger} to write to.
   */
  public static void dump(Logger logger) {
    logger.debug("***** Simulation settings: *****");
    logger.debug(">> Deployment interruption enabled: " + isDeploymentInterruptionEnabled());
  }

}
