package org.sapia.corus.cloud.aws.image.userdata;

/**
 * Runs the following command: 
 * <pre>
 * coruscli -c conf add -s s -p corus.server.ping.install-status=CLOUD_READY
 * </pre
 * 
 * @author yduchesne
 *
 */
public class AddCorusInstallStatusProperty implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    context.getUserData().line("coruscli -c conf add -s s -p corus.server.ping.install-status=CLOUD_READY");
  }

}
