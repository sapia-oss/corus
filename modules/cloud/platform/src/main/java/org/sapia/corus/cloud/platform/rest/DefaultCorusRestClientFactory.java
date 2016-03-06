package org.sapia.corus.cloud.platform.rest;

import org.sapia.corus.cloud.platform.domain.CorusAddress;

/**
 * Default implementation of the {@link CorusRestClientFactory} interface (creates {@link DefaultCorusRestClient}
 * instances).
 * 
 * @author yduchesne
 *
 */
public class DefaultCorusRestClientFactory implements CorusRestClientFactory {

  @Override
  public CorusRestClient getClient(CorusAddress address, CorusCredentials credentials) {
    return new DefaultCorusRestClient(address, credentials);
  }
}
