package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Installs Chef Solo under <code>/opt/chef/solo</code>.
 * 
 * @author yduchesne
 *
 */
public class InstallChefSolo implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    context.getUserData()
      .line("curl -L https://www.opscode.com/chef/install.sh | bash -s -- -v " + context.getConf().getChefVersion())
      .line("mkdir -p /opt/chef/solo")
      .line("cd /opt/chef")
      .line("wget http://github.com/opscode/chef-repo/tarball/master")
      .line("tar -zxf master")
      .line("mv chef-chef-repo* chef-repo")
      .line("rm master")
      .line("cd /root")
      .line("mkdir .chef")
      .line("echo \"cookbook_path [ '/opt/chef/chef-repo/cookbooks' ]\" > /root/.chef/knife.rb")
      .line("echo \"cookbook_path [ '/opt/chef/chef-repo/cookbooks' ]\" >> /opt/chef/solo/solo.rb")
      .line("echo \"file_cache_path '/opt/chef/solo'\" >> /opt/chef/solo/solo.rb")
      .line("git init /opt/chef/chef-repo/cookbooks")
      .line("cd /opt/chef/chef-repo/cookbooks")
      .line("git add .")
      .line("git commit -m \"Created Chef repo\"");
  }

}
