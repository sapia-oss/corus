package org.sapia.corus.cloud.platform.corus.rest;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Models credentials - to use with the {@link CorusRestClient}.
 * 
 * @author yduchesne
 *
 */
public class CorusCredentials {
  
  private String appId, appKey;
  
  public CorusCredentials(String appId, String appKey) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(appId), "Application ID must be specified");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(appKey), "Application key must be specified");

    this.appId  = appId;
    this.appKey = appKey;
  }
  
  /**
   * @return this instance's application key.
   */
  public String getAppKey() {
    return appKey;
  }
  
  /**
   * @return this instance's application ID.
   */
  public String getAppId() {
    return appId;
  }
}