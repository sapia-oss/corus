package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.processor.Process;

public class SpecificProcessRunningEvaluator implements ProcessConfigDiagnosticEvaluator {
  
  @Override
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context) {
    return context.getProcesses().size() == 1 && context.getExpectedInstanceCount() >=1 && context.isSpecificProcess();
  }
  
  @Override
  public void evaluate(ProcessConfigDiagnosticEvaluationContext context) {
    for (Process p : context.getProcesses()) {
      context.getResultsBuilder().results(context.getDiagnosticCallback().invoke(context, p));
    }
    String processConfigInfo = ToStringUtils.toString(context.getDistribution(), context.getProcessConfig());
    if (context.getLog().isInfoEnabled()) {
      context.getLog().info(String.format(
          "Found %s process(es) for %s (expected %s) - all processes are found running", 
          context.getProcesses().size(), processConfigInfo , context.getExpectedInstanceCount())
      );
    }    
  }

}
