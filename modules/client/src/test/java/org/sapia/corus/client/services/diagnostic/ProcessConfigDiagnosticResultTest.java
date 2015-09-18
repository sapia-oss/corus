package org.sapia.corus.client.services.diagnostic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;
import org.apache.commons.lang.SerializationUtils;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult.Builder;;

@RunWith(MockitoJUnitRunner.class)
public class ProcessConfigDiagnosticResultTest {
  
  @Mock
  private MutableClock     clock;
  private Builder          builder;
  private Distribution     dist;
  private DistributionInfo distInfo;
  private ProcessConfig    processConfig;
  
  @Mock
  private ProcessConfigDiagnosticEnv  context;
  
  // --------------------------------------------------------------------------
  // All expected processes running
  
  @Test
  public void testConfigStatus_failure_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_FAILED, "test", new Process(distInfo , "1")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.FAILURE, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_success_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.SUCCESS, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.NOOP, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_failure_multiple_expected_processes() {
    createFixtures(2);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_FAILED, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.FAILURE, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_success_multiple_expected_processes() {
    createFixtures(3);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG, "test", new Process(distInfo , "2")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_ACTIVE_PORT, "test", new Process(distInfo , "3")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.SUCCESS, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.NOOP, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_process_locked_multiple_expected_processes() {
    createFixtures(3);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG, "test", new Process(distInfo , "2")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "test", new Process(distInfo , "3")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.PENDING_EXECUTION, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.RETRY, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_process_locked_multiple_expected_processes_grace_period_exhausted() {
    createFixtures(3);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);

    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG, "test", new Process(distInfo , "2")));
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "test", new Process(distInfo , "3")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_diagnostic_config_undefined_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.NO_DIAGNOSTIC_AVAILABLE, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_no_active_port_undefined_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_ACTIVE_PORT, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.SUCCESS, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.NOOP, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_process_locked_undefined_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.PENDING_EXECUTION, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.RETRY, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_process_locked_undefined_single_expected_processes_grace_period_exhausted() {
    createFixtures(1);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);

    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  
  @Test
  public void testConfigStatus_process_suspect_single_expected_processes() {
    createFixtures(1);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.SUSPECT, "test", new Process(distInfo , "2")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.PENDING_EXECUTION, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.RETRY, result.getSuggestedAction());
  }
  
  
  @Test
  public void testConfigStatus_system_busy() {
    createFixtures(0);

    builder.status(ProcessConfigDiagnosticStatus.BUSY);
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.BUSY, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.RETRY, result.getSuggestedAction());
  }
  
  // --------------------------------------------------------------------------
  // No processes

  @Test
  public void testConfigStatus_no_expected_process() {
    createFixtures(0);
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.NO_PROCESSES_EXPECTED, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.NOOP, result.getSuggestedAction());
  }
  
  // --------------------------------------------------------------------------
  // Missing expected processes
  
  @Test
  public void testConfigStatus_failure_not_all_expected_processes() {
    createFixtures(2);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_FAILED, "test", new Process(distInfo , "1")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.FAILURE, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_success_pending_expected_processes() {
    createFixtures(2);
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.PENDING_EXECUTION, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.RETRY, result.getSuggestedAction());
  }
  
  @Test
  public void testConfigStatus_success_missing_expected_processes() {
    createFixtures(2);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);
    
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", new Process(distInfo , "1")));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
  
    assertEquals(ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES, result.getStatus());
    assertEquals(SuggestionDiagnosticAction.REMEDIATE, result.getSuggestedAction());
  }
  
  // --------------------------------------------------------------------------
  // Serialization test
  
  @Test
  public void testSerialization() {
    createFixtures(1);
 
    Process p =  new Process(distInfo , "1");
    p.setProcessDir("test");
    
    builder.results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", p));
    
    ProcessConfigDiagnosticResult result = builder.build(context);
   
    assertEquals(1, result.getProcessResults().size());
    
    byte[] payload = SerializationUtils.serialize(result);
    
    
    ProcessConfigDiagnosticResult copy = (ProcessConfigDiagnosticResult) SerializationUtils.deserialize(payload);
    
    assertEquals(result.getStatus(), copy.getStatus());
    assertEquals(result.getProcessConfig(), copy.getProcessConfig());
    assertEquals(result.getProcessResults().get(0), copy.getProcessResults().get(0));
  }
  
  // --------------------------------------------------------------------------
  // Fixtures
  
  private void createFixtures(int expectedCount) {
    final long startTime = 0;
    final TimeValue gracePeriod = TimeValue.createSeconds(60) ;
    builder       = Builder.newInstance();
    clock         = MutableClock.getInstance();
    dist          = new Distribution("test", "1.0");
    distInfo      = new DistributionInfo("test", "1.0", "test", "test");
    processConfig = new ProcessConfig("test");

    builder.distribution(dist).processConfig(processConfig);

    clock = MutableClock.getInstance();
    
    when(context.getDistribution()).thenReturn(dist);
    when(context.getExpectedInstanceCount()).thenReturn(expectedCount);
    when(context.getGracePeriod()).thenReturn(TimeValue.createSeconds(60));
    when(context.getProcessConfig()).thenReturn(processConfig);
    when(context.getProcesses()).thenReturn(new ArrayList<Process>());
    
    doAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return clock.currentTimeMillis() - startTime > gracePeriod.getValueInMillis() ;
      }
    }).when(context).isGracePeriodExhausted();
 
    doAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return clock.currentTimeMillis() - startTime <= gracePeriod.getValueInMillis() ;
      }
    }).when(context).isWithinGracePeriod();
 
  }

}
