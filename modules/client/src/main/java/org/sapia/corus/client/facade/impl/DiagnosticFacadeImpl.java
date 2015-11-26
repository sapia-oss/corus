package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DiagnosticFacade;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the {@link DiagnosticFacade} interface.
 * 
 * @author yduchesne
 *
 */
public class DiagnosticFacadeImpl  extends FacadeHelper<DiagnosticModule> implements DiagnosticFacade {
  
  private static final OptionalValue<LockOwner> NULL_LOCK_OWNER = OptionalValue.none();

  public DiagnosticFacadeImpl(CorusConnectionContext context) {
    super(context, DiagnosticModule.class);
  }
  
  @Override
  public Results<GlobalDiagnosticResult> acquireDiagnostics(
      ClusterInfo cluster) {
    Results<GlobalDiagnosticResult> results = new Results<GlobalDiagnosticResult>();
    proxy.acquireGlobalDiagnostics(NULL_LOCK_OWNER);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public Results<GlobalDiagnosticResult> acquireDiagnostics(
      ProcessCriteria criteria, ClusterInfo cluster) {
    Results<GlobalDiagnosticResult> results = new Results<GlobalDiagnosticResult>();
    proxy.acquireGlobalDiagnostics(criteria, NULL_LOCK_OWNER);
    invoker.invokeLenient(results, cluster);
    return results;
  }
}
