package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Outputs the recipe attributes to <code>/opt/chef/solo/attributes.json</code>
 * 
 * @author yduchesne
 *
 */
public class InstallRecipeAttributes implements UserDataPopulator {
  
  public void addTo(UserDataContext context) {
    context.getUserData().line(String.format("echo '%s\' >> /opt/chef/solo/attributes.json", context.getConf().getRecipeAttributes()));
  }

}
