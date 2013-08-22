package org.sapia.corus.cloud;

import java.io.IOException;
import java.net.URI;

import net.sf.json.JSONException;

/**
 * Interface specifying the behavior for fetching user data from
 * the cloud.
 * 
 * @author yduchesne
 *
 */
public interface CorusUserDataProvider {
  
  /**
   * @return the {@link CorusUserData} that was fetched.
   * @throws IOException if an problem occurs fetching the data.
   * @throws JSONException if a JSON parsing error occurred.
   */
  public CorusUserData fetchUserData() throws IOException, JSONException;
  
  /**
   * @param url a {@link URI}.
   * @return <code>true</code> if this instance can handle the provided {@link URI}.
   */
  public boolean accepts(URI url);
}
