package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DiagnosticFacade;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;

/**
 * Implements the {@link DiagnosticFacade} interface.
 * 
 * @author yduchesne
 *
 */
public class DiagnosticFacadeImpl  extends FacadeHelper<DiagnosticModule> implements DiagnosticFacade {

  public DiagnosticFacadeImpl(CorusConnectionContext context) {
    super(context, DiagnosticModule.class);
  }
  
  
  @Override
  public Results<GlobalDiagnosticResult> acquireDiagnostics(
      ClusterInfo cluster) {
    Results<GlobalDiagnosticResult> results = new Results<GlobalDiagnosticResult>();
    proxy.acquireDiagnostics();
    invoker.invokeLenient(results, cluster);
    return results;
  }
}
