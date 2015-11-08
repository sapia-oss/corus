package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Performs the following: <code>yum -y install git</code>
 * 
 * @author yduchesne
 *
 */
public class YumInstallGit implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    context.getUserData().line("yum -y install git");
  }
  
}
