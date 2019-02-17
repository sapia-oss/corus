package org.sapia.corus.aws.autoscaling;

import java.util.List;

/**
 * Defines configuration context of AWS auto-scaling group.
 * 
 * @author jcdesrochers
 */
public interface AwsAutoScalingConfiguration {

  public boolean isPartOfAutoScalingGroup();
  
  public String getAutoScalingGroupARN();
  
  public String getAutoScalingGroupName();
  
  public List<String> getElasticLoadBalancers();
  
  public boolean isInstanceProtetedFromScaleIn();
  
}
