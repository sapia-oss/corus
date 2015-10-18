package org.sapia.corus.processor.hook;

import java.io.IOException;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;

/**
 * Instances of this interface are invoked in order to start processes.
 * 
 * @author yduchesne
 *
 */
public interface ProcessStartHook {
  
  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to start.
   * @return <code>true</code> if this instance is able to handle to process to start.
   */
  public boolean accepts(ProcessContext context);

  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to start.
   * @param starterResult the {@link StarterResult} instance holding process startup information.
   * @param callback the {@link LogCallback} to use to write debug output.
   * @throws IOException if an I/O error occurs performing this operation.
   */
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException;
}
