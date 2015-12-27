package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.processor.Process;

/**
 * A {@link ProcessConfigDiagnosticEvaluator} that is invoked when only some processes are running, and we are still
 * within the grace period.
 * 
 * @author yduchesne
 *
 */
public class NotAllProcessesRunningWithinGracePeriodEvaluator implements ProcessConfigDiagnosticEvaluator {
  
  @Override
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context) {
    return !context.getProcesses().isEmpty() 
        && context.getProcesses().size() < context.getExpectedInstanceCount()
        && context.isWithinGracePeriod();
  }
  
  @Override
  public void evaluate(
      ProcessConfigDiagnosticEvaluationContext context) {
    for (Process p : context.getProcesses()) {
      context.getResultsBuilder().results(context.getDiagnosticCallback().invoke(context, p));
    }
    
    String processConfigInfo = ToStringUtil.toString(context.getDistribution(), context.getProcessConfig());
    context.getLog().warn(String.format(
        "Found %s process(es) for %s (expected %s) - some processes not yet started, execution deemed pending", 
        context.getProcesses().size(), processConfigInfo, context.getExpectedInstanceCount())
    );
  }

}
