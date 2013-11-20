package org.sapia.corus.cloud;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Holds Corus-specific user data.
 * 
 * @author yduchesne
 * 
 */
public class CorusUserData {

  private Properties serverProperties = new Properties();
  private Properties processProperties = new Properties();
  private Set<String> serverTags = new HashSet<String>();

  /**
   * @return this instance's server {@link Properties}.
   */
  public Properties getServerProperties() {
    return serverProperties;
  }

  /**
   * @return this instance's process {@link Properties}.
   */
  public Properties getProcessProperties() {
    return processProperties;
  }

  /**
   * @return this instance's {@link Set} of server tags.
   */
  public Set<String> getServerTags() {
    return serverTags;
  }

}
