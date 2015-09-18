package org.sapia.corus.diagnostic.evaluator;

import org.sapia.corus.diagnostic.DiagnosticModuleImpl;



/**
 * This interface is meant for subdividing the logic of the {@link DiagnosticModuleImpl} class, making the
 * behavior as a whole easier to test.
 * 
 * @author yduchesne
 *
 */
public interface ProcessConfigDiagnosticEvaluator {
  
   /**
   * Invoked to determine if this instance should be used.
   * 
   * @param context the current {@link ProcessConfigDiagnosticEvaluationContext}.
   * @return <code>true</code> if this instance is meant for proceed the diagnostic evaluation.
   */
  public boolean accepts(ProcessConfigDiagnosticEvaluationContext context);
  
  /**
   * Proceeds to the diagnostic evaluation, for processes corresponding to a given process configuration.
   * This method is invoked after {@link #accepts(ProcessConfigDiagnosticEvaluationContext)} itself has
   * been invoked and returned <code>true</code>.
   * 
   * @param context the current {@link ProcessConfigDiagnosticEvaluationContext}.
   */
  public void evaluate(ProcessConfigDiagnosticEvaluationContext context);
}


