package org.sapia.corus.client.facade;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;

/**
 * Specifies a facade corresponding to the {@link DiagnosticModule} interface.
 * 
 * @author yduchesne
 *
 */
public interface DiagnosticFacade {
  
  /**
   * @param cluster a {@link ClusterInfo}.
   * @return a {@link Results} instance holding {@link GlobalDiagnosticResult}s 
   * - one such result for each underlying Corus node.
   */
  public Results<GlobalDiagnosticResult> acquireDiagnostics(ClusterInfo cluster);
  
}
