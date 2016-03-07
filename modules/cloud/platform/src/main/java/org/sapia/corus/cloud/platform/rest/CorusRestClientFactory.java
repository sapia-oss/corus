package org.sapia.corus.cloud.platform.rest;

import org.sapia.corus.cloud.platform.domain.CorusAddress;

/**
 * Specifies the behavior for creating {@link CorusRestClient} instances.
 * 
 * @author yduchesne
 *
 */
public interface CorusRestClientFactory {
  
  /**
   * @param the {@link CorusAddress} corresponding to the address of the Corus server to connect to.
   * @param the {@link CorusCredentials} to use.
   * @return a new {@link CorusRestClient}.
   */
  public CorusRestClient getClient(CorusAddress address, CorusCredentials credentials);

}
