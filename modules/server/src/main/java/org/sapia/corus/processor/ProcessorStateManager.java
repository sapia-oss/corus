package org.sapia.corus.processor;

import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.corus.client.common.reference.ImmutableReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.event.ProcessKillPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartedEvent;
import org.sapia.ubik.rmi.interceptor.Interceptor;

/**
 * Manages the state of the {@link Processor} module.
 * 
 * @author yduchesne
 *
 */
public class ProcessorStateManager implements Interceptor {
  
  private Reference<ModuleState> state;
  private AtomicInteger          busyCount = new AtomicInteger();
  
  ProcessorStateManager(Reference<ModuleState> state, EventDispatcher toRegisterWith) {
    this.state = state;
    toRegisterWith.addInterceptor(ProcessKillPendingEvent.class, this);
    toRegisterWith.addInterceptor(ProcessKilledEvent.class, this);
    toRegisterWith.addInterceptor(ProcessRestartPendingEvent.class, this);
    toRegisterWith.addInterceptor(ProcessRestartedEvent.class, this);
 }
  
  /**
   * @return this instance's state.
   */
  public Reference<ModuleState> getState() {
    return ImmutableReference.of(state);
  }

  // --------------------------------------------------------------------------
  // Interception methods
  
  public synchronized void onProcessKillPendingEvent(ProcessKillPendingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }
  
  public synchronized void onProcessKilledEvent(ProcessKilledEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
  
  public synchronized void onProcessRestartPendingEvent(ProcessRestartPendingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }
  
  public synchronized void onProcessRestartedEvent(ProcessRestartedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
}
