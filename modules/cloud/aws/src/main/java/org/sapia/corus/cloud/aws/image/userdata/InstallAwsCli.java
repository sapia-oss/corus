package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Generates the <code>pip install awscli</code> commend.
 * 
 * @author yduchesne
 *
 */
public class InstallAwsCli implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    if (context.getSettings().getNotNull("isAwsCliInstall").get(Boolean.class)) {
      context.getUserData().line("pip install awscli");
    }
  }
  
}
