package org.sapia.corus.deployer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.dist.Distribution;
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
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.event.EventDispatcher;

@RunWith(MockitoJUnitRunner.class)
public class DeployerStateManagerTest {
  
  private Reference<ModuleState> state;
  
  @Mock
  private EventDispatcher        dispatcher;

  private DeployerStateManager   manager;
  
  private Distribution           dist;
  
  private DeploymentMetadata     metadata;
  
  @Before
  public void setUp() throws Exception {
    state = new DefaultReference<ModuleState>(ModuleState.IDLE);
    manager = new DeployerStateManager(state, dispatcher);
    dist = new Distribution("test", "1.0");
    
    metadata = new DeploymentMetadata("test", 0, ClusterInfo.clustered(), DeploymentMetadata.Type.FILE, DeployPreferences.newInstance()) {
    };
    
  }
  
  @Test
  public void testEventDispatcherRegistration() {
    verify(dispatcher).addInterceptor(eq(DeploymentStreamingStartingEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(DeploymentStreamingFailedEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(DeploymentStreamingCompletedEvent.class), eq(manager));
    
    verify(dispatcher).addInterceptor(eq(DeploymentStartingEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(DeploymentFailedEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(DeploymentCompletedEvent.class), eq(manager));
    
    verify(dispatcher).addInterceptor(eq(UndeploymentStartingEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(UndeploymentFailedEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(UndeploymentCompletedEvent.class), eq(manager));

    verify(dispatcher).addInterceptor(eq(RollbackStartingEvent.class), eq(manager));
    verify(dispatcher).addInterceptor(eq(RollbackCompletedEvent.class), eq(manager));
  }

  @Test
  public void testOnDeploymentStartingEvent() {
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    assertEquals(ModuleState.BUSY, state.get());
  }

  @Test
  public void testOnDeploymentUnzippedEvent() {
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentUnzippedEvent(new DeploymentUnzippedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentUnzippedEvent_concurrent_deployments() {
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentUnzippedEvent(new DeploymentUnzippedEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onDeploymentUnzippedEvent(new DeploymentUnzippedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());

  }

  @Test
  public void testOnDeploymentFailedEvent() {
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentFailedEvent(new DeploymentFailedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentFailedEvent_concurrent_deployments() {
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentStartingEvent(new DeploymentStartingEvent());
    manager.onDeploymentFailedEvent(new DeploymentFailedEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onDeploymentFailedEvent(new DeploymentFailedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentCompletedEvent() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onDeploymentCompletedEvent(new DeploymentCompletedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentCompletedEvent_concurrent_deployments() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onDeploymentCompletedEvent(new DeploymentCompletedEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onDeploymentCompletedEvent(new DeploymentCompletedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());
  }
  // --------------------------------------------------------------------------
  
  @Test
  public void testOnDeploymentStreamingStartingEvent() {
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    assertEquals(ModuleState.BUSY, state.get());
  }

  @Test
  public void testOnDeploymentStreamingFailedEvent() {
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingFailedEvent(new DeploymentStreamingFailedEvent(metadata));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentStreamingFailedEvent_concurrent_deployments() {
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingFailedEvent(new DeploymentStreamingFailedEvent(metadata));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onDeploymentStreamingFailedEvent(new DeploymentStreamingFailedEvent(metadata));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentStreamingCompletedEvent() {
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingCompletedEvent(new DeploymentStreamingCompletedEvent(metadata));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnDeploymentStreamingCompletedEvent_concurrent_deployments() {
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingStartingEvent(new DeploymentStreamingStartingEvent(metadata));
    manager.onDeploymentStreamingCompletedEvent(new DeploymentStreamingCompletedEvent(metadata));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onDeploymentStreamingCompletedEvent(new DeploymentStreamingCompletedEvent(metadata));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  // --------------------------------------------------------------------------


  @Test
  public void testOnRollbackStartingEvent() {
    manager.onRollbackStartingEvent(new RollbackStartingEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());
  }

  @Test
  public void testOnRollbackCompletedEvent() {
    manager.onRollbackStartingEvent(new RollbackStartingEvent(dist));
    manager.onRollbackCompletedEvent(new RollbackCompletedEvent(dist, RollbackCompletedEvent.Type.USER, RollbackCompletedEvent.Status.SUCCESS));
    assertEquals(ModuleState.IDLE, state.get());
  }
  
  @Test
  public void testOnRollbackCompletedEvent_concurrent_rollbacks() {
    manager.onRollbackStartingEvent(new RollbackStartingEvent(dist));
    manager.onRollbackStartingEvent(new RollbackStartingEvent(dist));
    manager.onRollbackCompletedEvent(new RollbackCompletedEvent(dist, RollbackCompletedEvent.Type.USER, RollbackCompletedEvent.Status.SUCCESS));
    assertEquals(ModuleState.BUSY, state.get());
    manager.onRollbackCompletedEvent(new RollbackCompletedEvent(dist, RollbackCompletedEvent.Type.USER, RollbackCompletedEvent.Status.SUCCESS));
    assertEquals(ModuleState.IDLE, state.get());

  }

  @Test
  public void testOnUndeploymentStartingEvent() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());  
  }

  @Test
  public void testOnUndeploymentFailedEvent() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentFailedEvent(new UndeploymentFailedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());  
  }
  
  @Test
  public void testOnUndeploymentFailedEvent_concurrent_undeployments() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentFailedEvent(new UndeploymentFailedEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());  
    manager.onUndeploymentFailedEvent(new UndeploymentFailedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());  
  }

  @Test
  public void testOnUndeploymentCompletedEvent() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentCompletedEvent(new UndeploymentCompletedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());    
  }
  
  @Test
  public void testOnUndeploymentCompletedEvent_concurrent_undeployments() {
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentStartingEvent(new UndeploymentStartingEvent(dist));
    manager.onUndeploymentCompletedEvent(new UndeploymentCompletedEvent(dist));
    assertEquals(ModuleState.BUSY, state.get());  
    manager.onUndeploymentCompletedEvent(new UndeploymentCompletedEvent(dist));
    assertEquals(ModuleState.IDLE, state.get());  
  }

}
