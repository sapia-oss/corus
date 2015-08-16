package org.sapia.corus.client.services.diagnostic;

import org.sapia.corus.client.Module;

/**
 * Allows acquiring diagnostic information from a Corus instance.
 * 
 * @author yduchesne
 *
 */
public interface DiagnosticModule extends java.rmi.Remote, Module {

  String ROLE = DiagnosticModule.class.getName();
  
  /**
   * Performs global diagnostics acquisition.
   * 
   * @return a {@link GlobalDiagnosticResult}.
   */
  public GlobalDiagnosticResult acquireDiagnostics();
  
}
