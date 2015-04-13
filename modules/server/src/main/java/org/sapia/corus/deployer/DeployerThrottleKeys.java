package org.sapia.corus.deployer;

import org.sapia.corus.taskmanager.core.DefaultThrottleKey;
import org.sapia.corus.taskmanager.core.ThrottleKey;

public class DeployerThrottleKeys {

  public static ThrottleKey DEPLOY_DISTRIBUTION            = new DefaultThrottleKey("DEPLOY_DISTRIBUTION");
  public static ThrottleKey UNDEPLOY_DISTRIBUTION          = new DefaultThrottleKey("UNDEPLOY_DISTRIBUTION");
  public static ThrottleKey ROLLBACK_DISTRIBUTION          = new DefaultThrottleKey("ROLLBACK_DISTRIBUTION");
  public static ThrottleKey UNARCHIVE_DISTRIBUTION         = new DefaultThrottleKey("UNARCHIVE_DISTRIBUTION");
  public static ThrottleKey DEPLOY_UNARCHIVED_DISTRIBUTION = new DefaultThrottleKey("DEPLOY_UNARCHIVED_DISTRIBUTION");
  
}
