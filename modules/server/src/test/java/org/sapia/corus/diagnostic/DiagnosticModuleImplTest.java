package org.sapia.corus.diagnostic;

import static org.junit.Assert.*
;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticCapable;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticStatus;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.event.ProcessStartPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessStartedEvent;
import org.sapia.corus.client.services.repository.Repository;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.diagnostic.DiagnosticModuleImpl.PendingProcessInfo;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluationContext;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluator;
import org.sapia.ubik.util.Collects;
import org.junit.runner.RunWith;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticModuleImplTest {
  
  private static final OptionalValue<LockOwner> NULL_LOCK_OWNER = OptionalValue.none();
  
  private Distribution       dist;
  private DistributionInfo   distInfo;
  private ProcessConfig      processConfig;
  private ProcessStartupInfo startupInfo;
  private Process            proc1, proc2, staleProc1, staleProc2;

  @Mock
  private ProcessConfigDiagnosticEvaluator rejectingEvaluator, acceptingEvaluator;

  @Mock
  private ProcessDiagnosticProvider rejectingProvider, acceptingProvider;
  
  @Mock
  private Deployer deployer;
  
  @Mock
  private Repository repository;
  
  @Mock
  private Processor processor;
  
  @Mock
  private SystemDiagnosticCapable sysProvider1, sysProvider2, sysProvider3;
  
  @Mock
  private InternalConfigurator configurator;
  
  private DiagnosticModuleImpl diagnosticModule;

  @Before
  public void setUp() throws Exception {
    
    dist          = new Distribution("test", "1.0");
    processConfig = new ProcessConfig("testProcess");
    startupInfo   = new ProcessStartupInfo(2);
    distInfo      = new DistributionInfo("test", "1.0", "testProfile", "testProcesss");
    proc1         = new Process(distInfo, "1");
    proc2         = new Process(distInfo, "2");
    staleProc1    = new Process(distInfo, "3");
    staleProc2    = new Process(distInfo, "4");
    
    dist.addProcess(processConfig);
    proc1.setStartupInfo(startupInfo);
    proc2.setStartupInfo(startupInfo);
    
    diagnosticModule = new DiagnosticModuleImpl();
    diagnosticModule.setConfigurator(configurator);
    diagnosticModule.setDeployer(deployer);
    diagnosticModule.setProcessor(processor);
    diagnosticModule.setProcessDiagnosticEvaluators(Collects.arrayToList(rejectingEvaluator, acceptingEvaluator));
    diagnosticModule.setProcessDiagnosticProviders(Collects.arrayToList(rejectingProvider, acceptingProvider));
    diagnosticModule.setSystemDiagnosticProviders(Collects.arrayToList(sysProvider1, sysProvider2, sysProvider3));

    when(deployer.getDistributions(any(DistributionCriteria.class))).thenReturn(Collects.arrayToList(dist));
    when(sysProvider1.getSystemDiagnostic()).thenReturn(new SystemDiagnosticResult("provider1", SystemDiagnosticStatus.UP));
    when(sysProvider2.getSystemDiagnostic()).thenReturn(new SystemDiagnosticResult("provider2", SystemDiagnosticStatus.UP));
    when(sysProvider3.getSystemDiagnostic()).thenReturn(new SystemDiagnosticResult("provider3", SystemDiagnosticStatus.UP));
    
    doAnswer(new Answer<List<Process>>() {
      @Override
      public List<Process> answer(InvocationOnMock invocation) throws Throwable {
        ProcessCriteria c = invocation.getArgumentAt(0, ProcessCriteria.class);
        if (c.getLifeCycles().contains(LifeCycleStatus.ACTIVE)) {
          return Arrays.asList(proc1, proc2);
        } else {
          return new ArrayList<Process>();
        }
      }
    }).when(processor).getProcesses(any(ProcessCriteria.class));
  
    when(rejectingEvaluator.accepts(any(ProcessConfigDiagnosticEvaluationContext.class))).thenReturn(false);
    when(acceptingEvaluator.accepts(any(ProcessConfigDiagnosticEvaluationContext.class))).thenReturn(true);
    
    when(rejectingProvider.accepts(any(DiagnosticContext.class))).thenReturn(false);
    when(acceptingProvider.accepts(any(DiagnosticContext.class))).thenReturn(true);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ProcessConfigDiagnosticEvaluationContext evalContext = invocation.getArgumentAt(0, ProcessConfigDiagnosticEvaluationContext.class);
        for (Process p : evalContext.getProcesses()) {
          evalContext.getResultsBuilder().results(Arrays.asList(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "success", p)));
        }
        return null;
      }
    }).when(acceptingEvaluator).evaluate(any(ProcessConfigDiagnosticEvaluationContext.class));
    
    when(acceptingProvider.performDiagnostic(any(DiagnosticContext.class))).thenReturn(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", proc1));
  }

 
  @Test
  public void testOnProcessStartPendingEvent() {
    ProcessStartPendingEvent event = new ProcessStartPendingEvent(dist, processConfig, startupInfo);
    diagnosticModule.onProcessStartPendingEvent(event);
   
    PendingProcessInfo pending = diagnosticModule.getPendingProcessInfoByGroup().get(startupInfo.getStartGroupId());
    
    assertNotNull(pending);
    assertEquals(0, pending.getCurrentlyStarted());
  }

  @Test
  public void testOnProcessStartedEvent() {
    ProcessStartPendingEvent pendingEvent = new ProcessStartPendingEvent(dist, processConfig, startupInfo);
    diagnosticModule.onProcessStartPendingEvent(pendingEvent);
   
    
    ProcessStartedEvent startedEvent = new ProcessStartedEvent(dist, processConfig, proc1);
    diagnosticModule.onProcessStartedEvent(startedEvent);
    
    PendingProcessInfo pending = diagnosticModule.getPendingProcessInfoByGroup().get(startupInfo.getStartGroupId());
    
    assertEquals(1, pending.getCurrentlyStarted());
  
    startedEvent = new ProcessStartedEvent(dist, processConfig, proc2);
    diagnosticModule.onProcessStartedEvent(startedEvent);
    
    assertTrue(diagnosticModule.getPendingProcessInfoByGroup().isEmpty());
  }
  
  @Test
  public void testGetExpectedInstances() {
    ProcessStartPendingEvent event = new ProcessStartPendingEvent(dist, processConfig, startupInfo);
    diagnosticModule.onProcessStartPendingEvent(event);
    
    assertEquals(2, diagnosticModule.getExpectedInstancesFor(processConfig));

    ProcessStartPendingEvent anotherEvent = new ProcessStartPendingEvent(dist, processConfig, ProcessStartupInfo.forSingleProcess());
    diagnosticModule.onProcessStartPendingEvent(anotherEvent);

    assertEquals(3, diagnosticModule.getExpectedInstancesFor(processConfig));
  }

  @Test
  public void testAcquireDiagnostic_all_processes_started() {
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    assertEquals(2, result.get(0).getProcessResults().size());
    
    assertEquals(ProcessConfigDiagnosticStatus.SUCCESS, result.get(0).getStatus());
  }
  
  @Test
  public void testAcquireDiagnostic_not_all_processes_started() {
    Mockito.reset(processor);
    when(processor.getState()).thenReturn(DefaultReference.of(ModuleState.IDLE));
    doAnswer(new Answer<List<Process>>() {
      @Override
      public List<Process> answer(InvocationOnMock invocation) throws Throwable {
        ProcessCriteria c = invocation.getArgumentAt(0, ProcessCriteria.class);
        if (!c.getLifeCycles().contains(LifeCycleStatus.ACTIVE)) {
          return new ArrayList<Process>();
        }
        return Arrays.asList(proc1);
      }
    }).when(processor).getProcesses(any(ProcessCriteria.class));
    
    ProcessStartPendingEvent event = new ProcessStartPendingEvent(dist, processConfig, startupInfo);
    diagnosticModule.onProcessStartPendingEvent(event);
    
    

    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    assertEquals(1, result.get(0).getProcessResults().size());
    assertEquals(ProcessConfigDiagnosticStatus.PENDING_EXECUTION, result.get(0).getStatus());
  }

  @Test
  public void testAcquireDiagnostic_no_tag_match() {
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("serverTag")));
    dist.setTags("distributionTag");
    processConfig.setTags("processTag");
    
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    assertTrue(result.get(0).getProcessResults().isEmpty());
    
    assertEquals(ProcessConfigDiagnosticStatus.NO_PROCESSES_EXPECTED, result.get(0).getStatus());
  }
  
  @Test
  public void testAcquireDiagnostic_sys_diagnostic_provider_busy() {
    when(sysProvider1.getSystemDiagnostic()).thenReturn(new SystemDiagnosticResult("sysProvider1", SystemDiagnosticStatus.BUSY));
    
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    assertTrue(result.get(0).getProcessResults().isEmpty());
    
    assertEquals(ProcessConfigDiagnosticStatus.BUSY, result.get(0).getStatus());
  }
  
  @Test
  public void testAcquireDiagnostic_stale_processes() {
    Mockito.reset(processor);
    when(processor.getState()).thenReturn(DefaultReference.of(ModuleState.IDLE));
    doAnswer(new Answer<List<Process>>() {
      @Override
      public List<Process> answer(InvocationOnMock invocation) throws Throwable {
        ProcessCriteria c = invocation.getArgumentAt(0, ProcessCriteria.class);
        if (c.getLifeCycles().contains(LifeCycleStatus.STALE)) {
          return Arrays.asList(staleProc1, staleProc2);
        } else if (c.getLifeCycles().contains(LifeCycleStatus.ACTIVE)) {
          return Arrays.asList(proc1, proc2);
        } else {
          return new ArrayList<Process>();
        }
      }
    }).when(processor).getProcesses(any(ProcessCriteria.class));

    
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    
    assertEquals(ProcessConfigDiagnosticStatus.FAILURE, result.get(0).getStatus());
  }
  
  @Test
  public void testAcquireDiagnostic_terminating_processes() {
    Mockito.reset(processor);
    when(processor.getState()).thenReturn(DefaultReference.of(ModuleState.IDLE));
    doAnswer(new Answer<List<Process>>() {
      @Override
      public List<Process> answer(InvocationOnMock invocation) throws Throwable {
        ProcessCriteria c = invocation.getArgumentAt(0, ProcessCriteria.class);
        if (c.getLifeCycles().contains(LifeCycleStatus.KILL_CONFIRMED) && c.getLifeCycles().contains(LifeCycleStatus.KILL_REQUESTED)) {
          return Arrays.asList(staleProc1, staleProc2);
        } else if (c.getLifeCycles().contains(LifeCycleStatus.ACTIVE)) {
          return Arrays.asList(proc1, proc2);
        } else {
          return new ArrayList<Process>();
        }
      }
    }).when(processor).getProcesses(any(ProcessCriteria.class));

    
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    
    assertEquals(ProcessConfigDiagnosticStatus.BUSY, result.get(0).getStatus());
  }
  
  @Test
  public void testAcquireDiagnostic_restarting_processes() {
    Mockito.reset(processor);
    when(processor.getState()).thenReturn(DefaultReference.of(ModuleState.IDLE));
    doAnswer(new Answer<List<Process>>() {
      @Override
      public List<Process> answer(InvocationOnMock invocation) throws Throwable {
        ProcessCriteria c = invocation.getArgumentAt(0, ProcessCriteria.class);
        if (c.getLifeCycles().contains(LifeCycleStatus.RESTARTING)) {
          return Arrays.asList(staleProc1, staleProc2);
        } else if (c.getLifeCycles().contains(LifeCycleStatus.ACTIVE)) {
          return Arrays.asList(proc1, proc2);
        } else {
          return new ArrayList<Process>();
        }
      }
    }).when(processor).getProcesses(any(ProcessCriteria.class));

    
    List<ProcessConfigDiagnosticResult> result = diagnosticModule.acquireProcessDiagnostics(NULL_LOCK_OWNER);
    
    assertEquals(ProcessConfigDiagnosticStatus.BUSY, result.get(0).getStatus());
  }
}
