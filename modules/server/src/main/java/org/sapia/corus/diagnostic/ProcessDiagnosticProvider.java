package org.sapia.corus.diagnostic;

import org.sapia.corus.client.services.deployer.dist.DiagnosticConfig;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;

/**
 * Provider interface that defines the behavior of instances capable of performing diagnostic for
 * given {@link DiagnosticConfig} instances.
 * 
 * @author yduchesne
 *
 */
public interface ProcessDiagnosticProvider {

  /**
   * @param context the current {@link DiagnosticContext}.
   * @return <code>true</code> if this instance accepts handling the {@link DiagnosticConfig} instance encpasulated
   * within the given context.
   */
  public boolean accepts(DiagnosticContext context);
  
  /**
   * Invoked if this instance's {@link #accepts(DiagnosticContext)} method has previously returned <code>true</code>,
   * for the given context.
   * 
   * @param context the current {@link DiagnosticContext}.
   * @return the {@link ProcessDiagnosticResult} obtains following diagnostic acquisition.
   */
  public ProcessDiagnosticResult performDiagnostic(DiagnosticContext context);
  
}
