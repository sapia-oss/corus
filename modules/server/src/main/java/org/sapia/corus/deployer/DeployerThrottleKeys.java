package org.sapia.corus.deployer;

import org.sapia.corus.taskmanager.core.DefaultThrottleKey;
import org.sapia.corus.taskmanager.core.ThrottleKey;

public class DeployerThrottleKeys {

  public static ThrottleKey DEPLOY_DISTRIBUTION            = new DefaultThrottleKey("Deployer:DeployDistribution");
  public static ThrottleKey UNDEPLOY_DISTRIBUTION          = new DefaultThrottleKey("Deployer:UndeployDistribution");
  public static ThrottleKey ROLLBACK_DISTRIBUTION          = new DefaultThrottleKey("Deployer:RollbackDistribution");
  public static ThrottleKey UNARCHIVE_DISTRIBUTION         = new DefaultThrottleKey("Deployer:UnarchiveDistribution");
  public static ThrottleKey DEPLOY_UNARCHIVED_DISTRIBUTION = new DefaultThrottleKey("Deployer:DeployUnarchivedDistribution");
  
}
