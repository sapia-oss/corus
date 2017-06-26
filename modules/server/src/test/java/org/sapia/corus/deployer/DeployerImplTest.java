package org.sapia.corus.deployer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticStatus;
import org.sapia.corus.taskmanager.core.TaskManager;

@RunWith(MockitoJUnitRunner.class)
public class DeployerImplTest {
  
  private TestServerContext ctx;
  
  private DeployerConfigurationImpl conf;
  
  @Mock
  private TaskManager taskMan;
  
  @Mock
  
  private DeployerImpl deployer;

  @Before
  public void setUp() throws Exception {
    ctx = TestServerContext.create();
    conf = new DeployerConfigurationImpl();
    deployer = new DeployerImpl();
    
    deployer.setEvents(ctx.getDisp());
    deployer.setConfiguration(conf);
    deployer.setTaskman(taskMan);
    deployer.setServerContext(ctx);
    
    deployer.init();
  }

  @Test
  public void testGetSystemDiagnostic_with_dist_absence_error() {
    deployer.setFlagDistAbsenceAsError(true);
    deployer.getStateManager().setState(ModuleState.IDLE);
    
    assertEquals(SystemDiagnosticStatus.DOWN, deployer.getSystemDiagnostic().getStatus());
  }

  @Test
  public void testGetSystemDiagnostic_with_dist_absence_not_flagged() {
    deployer.setFlagDistAbsenceAsError(false);
    deployer.getStateManager().setState(ModuleState.IDLE);
    
    assertEquals(SystemDiagnosticStatus.UP, deployer.getSystemDiagnostic().getStatus());
  }
  
  @Test
  public void testGetSystemDiagnostic_with_module_state_busy() {
    deployer.setFlagDistAbsenceAsError(false);
    deployer.getStateManager().setState(ModuleState.BUSY);
    
    assertEquals(SystemDiagnosticStatus.BUSY, deployer.getSystemDiagnostic().getStatus());
  }
}
