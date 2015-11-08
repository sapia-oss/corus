package org.sapia.corus.cloud.aws.image.userdata;

import org.sapia.corus.cloud.platform.settings.Settings;

public class UserDataContext {
  
  private UserDataBuilder userData  = UserDataBuilder.newInstance().executable();
  private Settings        settings;
  
  public UserDataContext(Settings settings) {
    this.settings = settings;
  }
  
  public UserDataBuilder getUserData() {
    return userData;
  }
  
  public Settings getSettings() {
    return settings;
  }

}
