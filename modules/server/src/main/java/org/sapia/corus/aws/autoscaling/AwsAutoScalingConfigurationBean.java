package org.sapia.corus.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.aws.AwsConfigBean;
import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.aws.AwsConfiguration.AwsConfigChangeListener;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.event.CorusEvent;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.AutoScalingInstanceDetails;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesResult;
import com.google.common.collect.Lists;

public class AwsAutoScalingConfigurationBean implements AwsAutoScalingConfiguration {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(AwsConfigBean.class.getName());
  
  @Autowired
  private InternalConfigurator  configurator;
  
  @Autowired
  private AwsConfiguration       awsConfiguration;

  private OptionalValue<String>  autoScalingGroupARN = OptionalValue.none();

  private OptionalValue<String>  autoScalingGroupName = OptionalValue.none();

  private OptionalValue<Boolean> isInstanceProtectedFromScaleIn = OptionalValue.none();
  
  private List<String>           loadBalancers = new ArrayList<>();
  
  private OptionalValue<AmazonAutoScaling> asgClient = OptionalValue.none();
  
  private Timer updateTimer;

  @PostConstruct
  public void init() {
    asgClient = OptionalValue.of(
        AmazonAutoScalingClientBuilder.
            standard().
            withRegion(awsConfiguration.getRegion()).
            build()
        );
    Timer updateTimer = new Timer("AwsAutoScalingConfigUpdateTimer", true);
    updateTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            updateAutoScalingConfig();
          }
        },
        5000L,
        1000L * 60 * 60);
  }
  
  @PreDestroy
  public void destroy() {
    if (asgClient.isSet()) {
      asgClient.get().shutdown();
      asgClient = OptionalValue.none();
    }
    
    if (updateTimer != null) {
      updateTimer.cancel();
    }
  }
  
  @Override
  public boolean isPartOfAutoScalingGroup() {
    return autoScalingGroupName.isSet();
  }

  @Override
  public String getAutoScalingGroupARN() {
    return autoScalingGroupARN.get();
  }

  @Override
  public String getAutoScalingGroupName() {
    return autoScalingGroupName.get();
  }

  @Override
  public boolean isInstanceProtetedFromScaleIn() {
    return isInstanceProtectedFromScaleIn.get();
  }

  @Override
  public List<String> getElasticLoadBalancers() {
    return loadBalancers;
  }
  
  public boolean updateAutoScalingConfig() {
    return doRetrieveAutoScalingInstanceInfo() && doRetrieveAutoScalingInfo();
  }
  
  protected boolean doRetrieveAutoScalingInstanceInfo() {
    DescribeAutoScalingInstancesRequest rq = new DescribeAutoScalingInstancesRequest().
        withInstanceIds(awsConfiguration.getInstanceId()).
        withMaxRecords(1).
        withSdkClientExecutionTimeout(5000).
        withSdkRequestTimeout(5000);

    DescribeAutoScalingInstancesResult rs = asgClient.get().describeAutoScalingInstances(rq);
    
    if (rs.getSdkHttpMetadata().getHttpStatusCode() == 200) {
      if (rs.getAutoScalingInstances().size() > 0) {
        AutoScalingInstanceDetails asgInstance = rs.getAutoScalingInstances().get(0);
        autoScalingGroupName = OptionalValue.of(asgInstance.getAutoScalingGroupName());
        isInstanceProtectedFromScaleIn = OptionalValue.of(asgInstance.getProtectedFromScaleIn());
      } else {
        autoScalingGroupName = OptionalValue.none();
        isInstanceProtectedFromScaleIn = OptionalValue.none();
      }
      return true;
    } else {
      return false;
    }
  }

  protected boolean doRetrieveAutoScalingInfo() {
    DescribeAutoScalingGroupsRequest rq = new DescribeAutoScalingGroupsRequest().
        withAutoScalingGroupNames(autoScalingGroupName.get()).
        withMaxRecords(1).
        withSdkClientExecutionTimeout(5000).
        withSdkRequestTimeout(5000);

    DescribeAutoScalingGroupsResult rs = asgClient.get().describeAutoScalingGroups(rq);
    
    if (rs.getSdkHttpMetadata().getHttpStatusCode() == 200) {
      if (rs.getAutoScalingGroups().size() > 0) {
        AutoScalingGroup asg = rs.getAutoScalingGroups().get(0);
        autoScalingGroupARN = OptionalValue.of(asg.getAutoScalingGroupARN());
        loadBalancers = Lists.newArrayList(asg.getLoadBalancerNames());
      } else {
        autoScalingGroupARN = OptionalValue.none();
        loadBalancers = Lists.newArrayList();
      }
      return true;
    } else {
      return false;
    }
  }
  
}
