package org.sapia.corus.processor.hook;

import java.io.IOException;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.os.OsModule.KillSignal;

/**
 * Instances of this interface are invoked in order to kill processes.
 * 
 * @author yduchesne
 *
 */
public interface ProcessKillHook {
  
  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to kill.
   * @return <code>true</code> if this instance is able to handle to process to kill.
   */
  public boolean accepts(ProcessContext context);

  /**
   * @param context a {@link ProcessContext} holding data corresponding to the process to kill.
   * @param the {@link KillSignal} to send to the process.
   * @param the {@link LogCallback} to use to write debug output.
   * @throws IOException if an I/O error occurs performing this operation.
   */
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException;
}
