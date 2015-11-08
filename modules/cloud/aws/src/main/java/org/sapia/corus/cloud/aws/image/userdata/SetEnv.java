package org.sapia.corus.cloud.aws.image.userdata;

/**
 * The following does two things:
 * 
 * <p>
 * 1) Sets the <code>JAVA_HOME</code> environment variable and adds <code>$JAVA_HOME/bin</code> to
 * the <code>PATH</code> environment variable.
 * <p>
 * 2) Sets the <code>CORUS_HOME</code> environment variable and adds <code>$CORUS_HOME/bin</code> to
 * the <code>PATH</code> environment variable.
 * <p>
 * The above is required since installation of Corus by Chef does not update the environment of the shell
 * in the context of which user data is being processed.
 * <p>
 * This step is used to make calling <code>coruscli</code> possible in the context of user data processing.
 * 
 * @author yduchesne
 *
 */
public class SetEnv implements UserDataPopulator {
  
  @Override
  public void addTo(UserDataContext context) {
    context.getUserData().line("export JAVA_HOME=/usr/lib/jvm/java");
    context.getUserData().line("export PATH=$PATH:$JAVA_HOME/bin");
    context.getUserData().line("export CORUS_HOME=/opt/corus/current");
    context.getUserData().line("export PATH=$PATH:$CORUS_HOME/bin");
  }

}
