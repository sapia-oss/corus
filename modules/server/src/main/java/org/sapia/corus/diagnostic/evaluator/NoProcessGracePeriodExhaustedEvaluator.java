package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.client.common.ToStringUtils;

/**
 * A {@link ProcessConfigDiagnosticEvaluator} that is invoked when no processes are running and the
 * grace period has passed.
 * 
 * @author yduchesne
 *
 */
public class NoProcessGracePeriodExhaustedEvaluator implements ProcessConfigDiagnosticEvaluator {
  
  @Override
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context) {
    return context.getProcesses().isEmpty() 
        && context.isGracePeriodExhausted();
  }
  
  @Override
  public void evaluate(ProcessConfigDiagnosticEvaluationContext context) {
    String processConfigInfo = ToStringUtils.toString(context.getDistribution(), context.getProcessConfig());
    if (context.getExpectedInstanceCount() == 0 && context.getLog().isDebugEnabled()) {
      context.getLog().info(String.format("No process(es) expected for %s", processConfigInfo));
    } else {
      context.getLog().warn(String.format(
          "No process(es) found for %s (expected %s) - grace period is exhausted, processes deemed missing", 
          processConfigInfo , context.getExpectedInstanceCount())
      );
    }
  }

}
