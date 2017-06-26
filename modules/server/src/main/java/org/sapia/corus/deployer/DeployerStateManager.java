package org.sapia.corus.deployer;

import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.corus.client.common.reference.ImmutableReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.event.DeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStartingEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStreamingCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStreamingFailedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStreamingStartingEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentUnzippedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackStartingEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentStartingEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.ubik.rmi.interceptor.Interceptor;

/**
 * Manages the {@link Deployer}'s state by listening to deployment events, adjusting the said
 * state accordingly.
 * 
 * @author yduchesne
 *
 */
public class DeployerStateManager implements Interceptor {

  private Reference<ModuleState> state;
  private AtomicInteger          busyCount = new AtomicInteger();
  
  DeployerStateManager(Reference<ModuleState> state, EventDispatcher toRegisterWith) {
    this.state = state;
    
    toRegisterWith.addInterceptor(DeploymentStreamingStartingEvent.class, this);
    toRegisterWith.addInterceptor(DeploymentStreamingFailedEvent.class, this);
    toRegisterWith.addInterceptor(DeploymentStreamingCompletedEvent.class, this);
    
    toRegisterWith.addInterceptor(DeploymentStartingEvent.class, this);
    toRegisterWith.addInterceptor(DeploymentUnzippedEvent.class, this);
    toRegisterWith.addInterceptor(DeploymentFailedEvent.class, this);
    toRegisterWith.addInterceptor(DeploymentCompletedEvent.class, this);
   
    toRegisterWith.addInterceptor(RollbackStartingEvent.class, this);
    toRegisterWith.addInterceptor(RollbackCompletedEvent.class, this);
    
    toRegisterWith.addInterceptor(UndeploymentStartingEvent.class, this);
    toRegisterWith.addInterceptor(UndeploymentFailedEvent.class, this);
    toRegisterWith.addInterceptor(UndeploymentCompletedEvent.class, this);
  }
  
  /**
   * @return this instance's state.
   */
  public Reference<ModuleState> getState() {
    return ImmutableReference.of(state);
  }

  // --------------------------------------------------------------------------
  // Deployment streaming
  
  public synchronized void onDeploymentStreamingStartingEvent(DeploymentStreamingStartingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }
  
  public synchronized void onDeploymentStreamingFailedEvent(DeploymentStreamingFailedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
  
  public synchronized void onDeploymentStreamingCompletedEvent(DeploymentStreamingCompletedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }  

  
  // --------------------------------------------------------------------------
  // Deployment
  
  public synchronized void onDeploymentStartingEvent(DeploymentStartingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }
  
  // we're considering the Deployer idle once a deployment is unzipped, since 
  // if the diags command is run from within the post-deploy script, it potentially
  // could loop forever, waiting that the Deployer becomes idle again
  public synchronized void onDeploymentUnzippedEvent(DeploymentUnzippedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
  
  public synchronized void onDeploymentFailedEvent(DeploymentFailedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
  
  public synchronized void onDeploymentCompletedEvent(DeploymentCompletedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
      busyCount.set(0);
    }
  }
  
  // --------------------------------------------------------------------------
  // Rollback
  
  public synchronized void onRollbackStartingEvent(RollbackStartingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }

  public synchronized void onRollbackCompletedEvent(RollbackCompletedEvent event) {
    if (busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
    }
  }

  // --------------------------------------------------------------------------
  // Undeployment

  public synchronized void onUndeploymentStartingEvent(UndeploymentStartingEvent event) {
    busyCount.incrementAndGet();
    state.set(ModuleState.BUSY);
  }
  
  public synchronized void onUndeploymentFailedEvent(UndeploymentFailedEvent event) {
    if(busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
    }
  }

  public synchronized void onUndeploymentCompletedEvent(UndeploymentCompletedEvent event) {
    if(busyCount.decrementAndGet() <= 0) {
      state.set(ModuleState.IDLE);
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  void setState(ModuleState newState) {
    state.set(newState);
  }
}
