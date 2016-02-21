package org.sapia.corus.processor.hook;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Collects;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class ProcessHookManagerImplTest {
  
  @Mock
  private ProcessKillHook k1, k2;
  
  @Mock
  private ProcessStartHook s1, s2;
  
  @Mock
  Map<String, ProcessKillHook> killHooks;
  
  @Mock
  Map<String, ProcessStartHook> startHooks;
  
  @Mock
  private ApplicationContext appContext;
 
  private Process process;
  
  private ProcessHookManagerImpl manager;
  
  @Before
  public void setUp() throws Exception {
    process = new Process(new DistributionInfo("dist", "1.0", "test", "test-proc"));
    
    manager = new ProcessHookManagerImpl();
    manager.setApplicationContext(appContext);

    when(k2.accepts(any(ProcessContext.class))).thenReturn(true);
    when(s2.accepts(any(ProcessContext.class))).thenReturn(true);
    when(killHooks.values()).thenReturn(Collects.arrayToList(k1, k2));
    when(startHooks.values()).thenReturn(Collects.arrayToList(s1, s2));
    when(appContext.getBeansOfType(ProcessKillHook.class)).thenReturn(killHooks);
    when(appContext.getBeansOfType(ProcessStartHook.class)).thenReturn(startHooks);

    manager.init();
  }

  @Test
  public void testKill() throws Exception {
    manager.kill(new ProcessContext(process), KillSignal.SIGKILL, mock(LogCallback.class));
  }

  @Test
  public void testStart() throws Exception {
    manager.start(new ProcessContext(process), new StarterResult(StarterType.JAVA, new CmdLine(), true), mock(LogCallback.class));
  }

}
