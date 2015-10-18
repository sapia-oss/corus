package org.sapia.corus.processor.hook;

import java.io.IOException;
import java.util.Set;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.ubik.util.Collects;

/**
 * Default implementation of the {@link ProcessKillHook}. Handles the following starter types:
 * 
 * <ul>
 *  <li> {@link StarterType#GENERIC}
 *  <li> {@link StarterType#JAVA}
 *  <li> {@link StarterType#MAGNET}
 * </ul>
 * 
 * @author yduchesne
 *
 */
public class DefaultProcessKillHook implements ProcessKillHook {
  
  private static final Set<StarterType> ACCEPTED_STARTER_TYPES = Collects.arrayToSet(StarterType.GENERIC, StarterType.JAVA, StarterType.MAGNET);
  
  private OsModule os;
  
  /// -------------------------------------------------------------------------
  // Visible for testing
  
  public void setOs(OsModule os) {
    this.os = os;
  }
  
  // --------------------------------------------------------------------------
  // ProcessKillHook interface
  
  @Override
  public boolean accepts(ProcessContext context) {
    return ACCEPTED_STARTER_TYPES.contains(context.getProcess().getStarterType());
  }
  
  @Override
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException {
    os.killProcess(callback, signal, context.getProcess().getOsPid());
  }

}
