package org.sapia.corus.processor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.event.ProcessKillPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartedEvent;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorStateManagerTest {
  
  @Mock
  private EventDispatcher        dispatcher;
  
  private Distribution           dist;
  private ProcessConfig          processConfig;
  private Process                process;
  
  private Reference<ModuleState> state;
  private ProcessorStateManager  manager;

  @Before
  public void setUp() throws Exception {
    dist    = new Distribution("test", "1.0");
    processConfig = new ProcessConfig("test");
    process = new Process(new DistributionInfo("test", "1.0", "testProfile", "testProcess"));
    state   = new DefaultReference<ModuleState>(ModuleState.IDLE);
    manager = new ProcessorStateManager(state, dispatcher);
  }

  @Test
  public void testOnProcessKillPendingEvent() {
    manager.onProcessKillPendingEvent(
        new ProcessKillPendingEvent(
            ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
            process
        )
    );
    
    assertEquals(ModuleState.BUSY, state.get());
  }
  
   @Test
  public void testOnProcessKilledEvent() {
     manager.onProcessKillPendingEvent(
         new ProcessKillPendingEvent(
             ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
             process
         )
     );
     manager.onProcessKilledEvent(
         new ProcessKilledEvent(
             ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
             process, false
         )
     );
     assertEquals(ModuleState.IDLE, state.get());
  }

   
   @Test
   public void testOnProcessRestartPendingEvent() {
     manager.onProcessRestartPendingEvent(
         new ProcessRestartPendingEvent(
             dist, processConfig,
             process
         )
     );
     
     assertEquals(ModuleState.BUSY, state.get());
   }
   
    @Test
   public void testOnProcessRestartedEvent() {
      manager.onProcessRestartPendingEvent(
          new ProcessRestartPendingEvent(
              dist, processConfig,
              process
          )
      );
      manager.onProcessRestartedEvent(
          new ProcessRestartedEvent(
              dist, processConfig,
              process
          )
      );
   
      assertEquals(ModuleState.IDLE, state.get());
   }
}
