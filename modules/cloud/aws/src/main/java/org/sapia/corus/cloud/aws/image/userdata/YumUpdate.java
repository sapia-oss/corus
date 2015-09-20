package org.sapia.corus.cloud.aws.image.userdata;

/**
 * 
 * @author yduchesne
 *
 */
public class YumUpdate implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    if (context.getConf().isYumUpdate()) {
      context.getUserData().line("yum -y update");
    }
  }

}
