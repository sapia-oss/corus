package org.sapia.corus.diagnostic.evaluator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult.Builder;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.diagnostic.ProcessDiagnosticCallback;
import org.sapia.ubik.util.SysClock.MutableClock;

class ProcessConfigDiagnosticEvaluatorTestHelper {
  
  @Mock
  ProcessDiagnosticCallback                callback;
  MutableClock                             clock;
  ProcessConfigDiagnosticEvaluationContext context;
  List<Process>                            processes;
  ProcessStartupInfo                       startupInfo;
  ProcessConfigDiagnosticResult.Builder    results;
  long                                     startTime;
  
  void createFixtures(int expectedCount, int actualProcessCount) {
    startupInfo = new ProcessStartupInfo(expectedCount);
    processes = new ArrayList<Process>();
     clock = MutableClock.getInstance();
    ProcessConfig conf = new ProcessConfig("testProcess");
    for (int i = 0; i < actualProcessCount; i++) {
      DistributionInfo dist = new DistributionInfo("dist", "1.0", "test", "testProcess");
      Process p = new Process(dist);
      p.setOsPid("" + (i + 1));
      p.setStartupInfo(startupInfo);
      processes.add(p);
    }
    results = Builder.newInstance().distribution(new Distribution("dest", "1.0")).processConfig(conf);
    context = new ProcessConfigDiagnosticEvaluationContext(callback, results, new Distribution("dist", "1.0"), conf, processes, expectedCount)
      .withClock(clock).withStartTime(startTime);
    
    doAnswer(new Answer<List<ProcessDiagnosticResult>>() {
      @Override
      public List<ProcessDiagnosticResult> answer(InvocationOnMock invocation)
          throws Throwable {
        Process p = (Process) invocation.getArgumentAt(1, Process.class);
        return Arrays.asList(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "test", p));
      }
    }).when(callback).invoke(any(ProcessConfigDiagnosticEvaluationContext.class), any(Process.class));
  }

}
