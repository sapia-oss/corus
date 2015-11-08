package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Runs the Chef solo command line, using the <code>solo.rb</code> file under <code>/opt/chef/solo/solo.rb</code>
 * and the run lists (and attributes) configured in <code>/opt/chef/solo/attributes.json</code>.
 * 
 * @author yduchesne
 *
 */
public class RunChefSolo implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    context.getUserData().line("chef-solo -c /opt/chef/solo/solo.rb -j /opt/chef/solo/attributes.json");    
  }

}
