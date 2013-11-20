package org.sapia.corus.cloud;

import java.net.URI;

/**
 * Fetches user data from the predefined AWS endpoint.
 * 
 * @author yduchesne
 * 
 */
public class OpenStackCorusUserDataProvider extends CorusUserDataProviderSupport {

  private static final URI USER_DATA_URI = URI.create("http://169.254.169.254/openstack/latest/user-data");

  public OpenStackCorusUserDataProvider() {
    super(USER_DATA_URI);
  }

}
