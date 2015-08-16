package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.processor.Process;

/**
 * A {@link ProcessConfigDiagnosticEvaluator} that is invoked when only some processes are running, 
 * and we are still within the grace period.
 * 
 * @author yduchesne
 *
 */
public class NotAllProcessesRunningGracePeriodExhaustedEvaluator implements ProcessConfigDiagnosticEvaluator {
  
  @Override
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context) {
    return !context.getProcesses().isEmpty() 
        && context.getProcesses().size() < context.getExpectedInstanceCount()
        && context.isGracePeriodExhausted();
  }
  
  @Override
  public void evaluate(
      ProcessConfigDiagnosticEvaluationContext context) {
    for (Process p : context.getProcesses()) {
      context.getResultsBuilder().results(context.getDiagnosticCallback().invoke(context, p));
    }
    
    String processConfigInfo = ToStringUtils.toString(context.getDistribution(), context.getProcessConfig());
    context.getLog().warn(String.format(
        "Found %s process(es) for %s (expected %s). Grace period has passed, processes are deemed missing", 
        context.getProcesses().size(), processConfigInfo, context.getExpectedInstanceCount())
    );
  }

}
