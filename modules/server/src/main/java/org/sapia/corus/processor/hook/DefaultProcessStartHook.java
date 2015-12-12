package org.sapia.corus.processor.hook;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.ubik.util.Collects;
import org.springframework.beans.factory.annotation.Autowired;

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
public class DefaultProcessStartHook implements ProcessStartHook {

  private static final Set<StarterType> ACCEPTED_STARTER_TYPES = Collects.arrayToSet(StarterType.GENERIC, StarterType.JAVA, StarterType.MAGNET);

  @Autowired
  private OsModule os;

  /// -------------------------------------------------------------------------
  // Visible for testing

  public void setOs(OsModule os) {
    this.os = os;
  }

  // --------------------------------------------------------------------------
  // ProcessStartHook interface

  @Override
  public boolean accepts(ProcessContext context) {
    return ACCEPTED_STARTER_TYPES.contains(context.getProcess().getStarterType());
  }

  @Override
  public void start(ProcessContext context, StarterResult starterResult,
      LogCallback callback) throws IOException {
    String pid = os.executeProcess(callback, new File(context.getProcess().getProcessDir()), starterResult.getCommand(), new HashMap<String, String>());
    context.getProcess().setOsPid(pid);
  }

}
