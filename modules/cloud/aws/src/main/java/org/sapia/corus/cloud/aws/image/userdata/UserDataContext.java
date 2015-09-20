package org.sapia.corus.cloud.aws.image.userdata;

import org.sapia.corus.cloud.aws.image.EC2ImageConf;

public class UserDataContext {
  
  private UserDataBuilder userData  = UserDataBuilder.newInstance().executable();
  private EC2ImageConf    conf;
  
  public UserDataContext(EC2ImageConf conf) {
    this.conf = conf;
  }
  
  public UserDataBuilder getUserData() {
    return userData;
  }
  
  public EC2ImageConf getConf() {
    return conf;
  }

}
