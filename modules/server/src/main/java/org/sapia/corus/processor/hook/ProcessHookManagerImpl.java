package org.sapia.corus.processor.hook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.util.Assertions;

/**
 * Implementation of the {@link ProcessHookManager} interface.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = { ProcessHookManager.class })
public class ProcessHookManagerImpl extends ModuleHelper implements ProcessHookManager {
  
  private List<ProcessStartHook> startHooks = new ArrayList<ProcessStartHook>();
  private List<ProcessKillHook>  killHooks  = new ArrayList<ProcessKillHook>();
  
  // --------------------------------------------------------------------------
  // Module methods

  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  @Override
  public void init() {
    startHooks.addAll(appContext.getBeansOfType(ProcessStartHook.class).values());
    killHooks.addAll(appContext.getBeansOfType(ProcessKillHook.class).values());
    
    Assertions.illegalState(startHooks.isEmpty(), "No ProcessStartHook configured");
    Assertions.illegalState(killHooks.isEmpty(), "No ProcessKillHook configured");
    
    for (ProcessStartHook h : startHooks) {
      logger().info("Got ProcessStartHook: " + h);
    }
    
    for (ProcessKillHook h : killHooks) {
      logger().info("Got ProcessKillHook: " + h);
    }
    
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // ProcessHookManager interface
  
  @Override
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException {
    logger().debug("Proceeding to killing of process: " + ToStringUtil.toString(context.getProcess()));
    for (ProcessKillHook hook : killHooks) {
      if (hook.accepts(context)) {
        hook.kill(context, signal, callback);
        return;
      }
    }
    throw new IllegalStateException("Could not find ProcessKillHook for process: " + ToStringUtil.toString(context.getProcess()));
  }
  
  @Override
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException {
    logger().debug("Proceeding to startup of process: " + ToStringUtil.toString(context.getProcess()));
    for (ProcessStartHook hook : startHooks) {
      if (hook.accepts(context)) {
        hook.start(context, starterResult, callback);
        return;
      }
    }
    throw new IllegalStateException("Could not find ProcessStartHook for process: " + ToStringUtil.toString(context.getProcess()));    
  }

}
