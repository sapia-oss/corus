package org.sapia.corus.client.services.diagnostic;

/**
 * Implemented by components capable of providing a diagnostic for themselves.
 * 
 * @author yduchesne
 *
 */
public interface SystemDiagnosticCapable {

  /**
   * @return the {@link SystemDiagnosticResult} that this instance has evaluated.
   */
  public SystemDiagnosticResult getSystemDiagnostic();
  
}
