package org.sapia.corus.diagnostic;

import java.util.List;

import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluationContext;

/**
 * Meant to isolate implementation details of process diagnostic acquisition.
 * 
 * @author yduchesne
 *
 */
public interface ProcessDiagnosticCallback {
  
  /**
   * @param context the current {@link ProcessConfigDiagnosticEvaluationContext}.
   * @param toDiagnose the {@link Process} to diagnose.
   * @return the list of {@link ProcessDiagnosticResult}s resulting from this operation.
   */
  public List<ProcessDiagnosticResult> invoke(
      ProcessConfigDiagnosticEvaluationContext context, 
      Process toDiagnose);
  
}
