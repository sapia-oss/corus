package org.sapia.corus.cloud;

import java.net.URI;

/**
 * Fetches user data from the predefined AWS endpoint.
 * 
 * @author yduchesne
 * 
 */
public class AwsCorusUserDataProvider extends CorusUserDataProviderSupport {

  private static final URI USER_DATA_URI = URI.create("http://169.254.169.254/latest/user-data");

  public AwsCorusUserDataProvider() {
    super(USER_DATA_URI);
  }

}
