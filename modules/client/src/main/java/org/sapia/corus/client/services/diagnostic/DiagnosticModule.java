package org.sapia.corus.client.services.diagnostic;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;

/**
 * Allows acquiring diagnostic information from a Corus instance.
 * 
 * @author yduchesne
 *
 */
public interface DiagnosticModule extends java.rmi.Remote, Module {

  String ROLE = DiagnosticModule.class.getName();
  
  /**
   * Performs global diagnostics acquisition (includes acquiring Corus-specific diagnostic).
   * 
   * @return a {@link GlobalDiagnosticResult}.
   */
  public GlobalDiagnosticResult acquireDiagnostics();
  
  /**
   * Performs process diagnostic acquisition (excludes acquiring Corus-specific diagnostic).
   * 
   * @return the {@link List} of {@link ProcessConfigDiagnosticResult}s resulting from this operation.
   */
  public List<ProcessConfigDiagnosticResult> acquireProcessDiagnostics();
  
  /**
   * This method acquires the diagnostic for the given process. The returned result will correspond
   * to "incomplete" if the current lock owner of the process does not correspond to the one passed in.
   * 
   * @param process a {@link Process} whose diagnostic to acquire.
   * @param requestingOwner the {@link LockOwner} of the party requesting the diagnostic.
   * @return the {@link ProcessDiagnosticResult} corresponding to the given process.
   */
  public ProcessDiagnosticResult acquireDiagnosticFor(Process process, LockOwner requestingOwner);
  
}
