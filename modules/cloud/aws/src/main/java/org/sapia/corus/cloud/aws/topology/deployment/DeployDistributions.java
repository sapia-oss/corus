package org.sapia.corus.cloud.aws.topology.deployment;

import java.util.List;

import org.sapia.corus.cloud.aws.topology.deployment.helper.DeploymentStepSupport;
import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.util.Input;

import com.google.common.base.Function;

public class DeployDistributions extends DeploymentStepSupport {
  
  private static final String DESC = "deploying distributions";

  public DeployDistributions() {
    super(
        DESC,
        new Function<AwsTopologyDeploymentContext, List<Input>>() {
          @SuppressWarnings("unchecked")
          @Override
          public List<Input> apply(AwsTopologyDeploymentContext context) {
            return (List<Input>) context.getSettings().get("distributions").get(List.class);
          }
        },
        new Function<CorusInstance, String>() {
          @Override
          public String apply(CorusInstance instance) {
            return "/clusters/" + instance.getCluster().getName() + "/distributions";
          }
        }
    );
  }

}
