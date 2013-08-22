package org.sapia.corus.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

/**
 * A support class for implementing {@link CorusUserDataProvider}s: an instance
 * of this class expects a URL corresponding to the Cloud provider's user data endpoint.
 * 
 * @author yduchesne
 *
 */
public abstract class CorusUserDataProviderSupport implements CorusUserDataProvider {

  private URI userDataUrl;
  
  /**
   * @param userDataUrl the user data {@link URI}.
   */
  protected CorusUserDataProviderSupport(URI userDataUrl) {
    this.userDataUrl = userDataUrl;
  }
  
  @Override
  public CorusUserData fetchUserData() throws IOException {
    URLConnection connection = userDataUrl.toURL().openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(false);
    connection.setUseCaches(false);
    
    InputStream is = connection.getInputStream();
    
    try {
      return CorusUserDataParser.parse(is);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  @Override
  public boolean accepts(URI url) {
    return userDataUrl.equals(url);
  }
}
