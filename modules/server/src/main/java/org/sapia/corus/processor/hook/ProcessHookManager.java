package org.sapia.corus.processor.hook;

import java.io.IOException;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.os.OsModule.KillSignal;

/**
 * Impplementation is meant to internally chain {@link ProcessKillHook} and {@link ProcessStartHook}
 * to fulfill contract.
 * 
 * @author yduchesne
 *
 */
public interface ProcessHookManager {
  
  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to kill.
   * @param the {@link KillSignal} to send to the process.
   * @param the {@link LogCallback} to use to write debug output.
   * @throws IOException if an I/O error occurs performing this operation.
   */
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException;

  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to start.
   * @param starterResult the {@link StarterResult} instance holding process startup information.
   * @param callback the {@link LogCallback} to use to write debug output.
   * @throws IOException if an I/O error occurs performing this operation.
   */
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException;

}
