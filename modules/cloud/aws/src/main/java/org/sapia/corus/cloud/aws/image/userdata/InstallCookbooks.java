package org.sapia.corus.cloud.aws.image.userdata;

import org.sapia.corus.cloud.aws.image.EC2ImageConf;

/**
 * Generates cookbook installation commands, based on Chef's <code>knife</code> command.
 * 
 * @see EC2ImageConf#getCookbooks()
 * @author yduchesne
 *
 */
public class InstallCookbooks implements UserDataPopulator {
 
  @Override
  public void addTo(UserDataContext context) {
 
    for (String c : context.getConf().getCookbooks()) {
      context.getUserData().line("knife cookbook site install " + c + " --config /opt/chef/solo/solo.rb");
    }
    
  }
 
}
