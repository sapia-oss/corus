package org.sapia.corus.cloud.aws.image.userdata;

import org.sapia.corus.cloud.aws.image.creation.ImageCreationConf;

/**
 * Generates cookbook installation commands, based on Chef's <code>knife</code> command.
 * 
 * @see ImageCreationConf#getCookbooks()
 * @author yduchesne
 *
 */
public class InstallCookbooks implements UserDataPopulator {
 
  @Override
  public void addTo(UserDataContext context) {
 
    for (String c : context.getSettings().getNotNull("cookbook").getListOf(String.class)) {
      context.getUserData().line("knife cookbook site install " + c + " --config /opt/chef/solo/solo.rb");
    }
    
  }
 
}
