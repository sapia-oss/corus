package org.sapia.corus.cloud;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.sapia.ubik.util.Collects;

/**
 * Provides methods for retrieving {@link CorusUserData} from predefined
 * {@link CorusUserDataProvider}s.
 * 
 * @author yduchesne
 * 
 */
public class CorusUserDataFactory {

  private static final List<? extends CorusUserDataProvider> PROVIDERS = Collects.arrayToList(new AwsCorusUserDataProvider());

  private CorusUserDataFactory() {
  }

  /**
   * This method internally loops through every built-in
   * {@link CorusUserDataProvider} in order to fetch the {@link CorusUserData}
   * from the appropriate provider.
   * 
   * @return the fetched {@link CorusUserData}.
   * @throws IOException
   *           if the user data could not be fetched.
   */
  public static CorusUserData fetchUserData() throws IOException {

    IOException userDataError = null;
    for (CorusUserDataProvider p : PROVIDERS) {
      try {
        return p.fetchUserData();
      } catch (IOException e) {
        userDataError = e;
      }
    }
    if (userDataError != null) {
      throw userDataError;
    }
    throw new IOException("Could not fetch user data");
  }

  /**
   * @param url
   *          the {@link URL} from which to fetch user data.
   * @return the fetched {@link CorusUserData}.
   * @throws IOException
   *           if the user data could not be fetched.
   */
  public static CorusUserData fetchUserData(URI url) throws IOException {
    for (CorusUserDataProvider p : PROVIDERS) {
      if (p.accepts(url)) {
        return p.fetchUserData();
      }
    }
    return new AnonymousCorusUserDataProvider(url).fetchUserData();
  }

  // ==========================================================================

  static final class AnonymousCorusUserDataProvider extends CorusUserDataProviderSupport {

    public AnonymousCorusUserDataProvider(URI url) {
      super(url);
    }
  }

}
