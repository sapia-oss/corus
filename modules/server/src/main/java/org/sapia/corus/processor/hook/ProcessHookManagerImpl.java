package org.sapia.corus.processor.hook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of the {@link ProcessHookManager} interface.
 * 
 * @author yduchesne
 *
 */
public class ProcessHookManagerImpl implements ProcessHookManager, ApplicationContextAware {
  
  private ApplicationContext     appContext;
  private List<ProcessStartHook> startHooks = new ArrayList<ProcessStartHook>();
  private List<ProcessKillHook>  killHooks  = new ArrayList<ProcessKillHook>();
  
  @Override
  public void setApplicationContext(ApplicationContext appContext)
      throws BeansException {
    this.appContext = appContext;
  }
  
  @PostConstruct
  public void init() {
    startHooks.addAll(appContext.getBeansOfType(ProcessStartHook.class).values());
    killHooks.addAll(appContext.getBeansOfType(ProcessKillHook.class).values());
  }
  
  @Override
  public void kill(ProcessContext context, KillSignal signal, LogCallback callback) throws IOException {
    for (ProcessKillHook hook : killHooks) {
      if (hook.accepts(context)) {
        hook.kill(context, signal, callback);
        return;
      }
    }
    throw new IllegalStateException("Could not find ProcessKillHook for process: " + ToStringUtils.toString(context.getProcess()));
  }
  
  @Override
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException {
    for (ProcessStartHook hook : startHooks) {
      if (hook.accepts(context)) {
        hook.start(context, starterResult, callback);
        return;
      }
    }
    throw new IllegalStateException("Could not find ProcessStartHook for process: " + ToStringUtils.toString(context.getProcess()));    
  }

}
