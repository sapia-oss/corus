package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.client.common.ToStringUtil;

/**
 * A {@link ProcessConfigDiagnosticEvaluator} that is invoked when no processes are running and the
 * grace period has NOT yet passed.
 * 
 * @author yduchesne
 *
 */
public class NoProcessWithinGracePeriodEvaluator implements ProcessConfigDiagnosticEvaluator {
  
  @Override
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context) {
    return context.getProcesses().isEmpty() 
        && context.isWithinGracePeriod();
  }
  
  @Override
  public void evaluate(ProcessConfigDiagnosticEvaluationContext context) {
    String processConfigInfo = ToStringUtil.toString(context.getDistribution(), context.getProcessConfig());
    if (context.getExpectedInstanceCount() == 0 && context.getLog().isDebugEnabled()) {
      context.getLog().debug(String.format("No process(es) expected for %s", processConfigInfo));
    } else {
      context.getLog().warn(String.format(
          "No process(es) yet found for %s (expected %s) - process execution deemed pending", 
          processConfigInfo , context.getExpectedInstanceCount())
      );
    }
  }

}
