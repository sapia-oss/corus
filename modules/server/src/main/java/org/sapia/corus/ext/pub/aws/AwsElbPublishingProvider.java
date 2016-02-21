package org.sapia.corus.ext.pub.aws;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.aws.AwsConfiguration.AwsConfigChangeListener;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.deployer.dist.AwsElbPublisherConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.publisher.ProcessPublishingProvider;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Func;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;

/**
 * Implements a {@link ProcessPublishingProvider} that publish processes to given AWS elastic load-balancers.
 * <p>
 * This class implements connection draining: it will check with the ELB for instance deregistration completion.
 * 
 * @author yduchesne
 *
 */
public class AwsElbPublishingProvider extends ModuleHelper implements ProcessPublishingProvider, AwsConfigChangeListener {
  
  public static final String ROLE = ProcessPublishingProvider.class.getName() + ".AWS";


  @Autowired
  private AwsConfiguration awsConf;
  
  private OptionalValue<AmazonElasticLoadBalancing> elbClient = OptionalValue.none();
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setAwsConfiguration(AwsConfiguration conf) {
    this.awsConf = conf;
  }
  
  void setElbClient(AmazonElasticLoadBalancing elbClient) {
    this.elbClient = OptionalValue.of(elbClient);
  }
  
  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    awsConf.addConfigChangeListener(this);
    if (awsConf.isAwsEnabled()) {
      log.info("AWS integration is enabled, creating AWS client");
      elbClient = OptionalValue.of(createElbClient());
    } else {
      log.info("AWS integration is disabled. AWS client not created");
    }
  }
  
  @Override
  public void dispose() throws Exception {
    if (awsConf.isAwsEnabled()) {
      elbClient.get().shutdown();
    }
  }
  
  // --------------------------------------------------------------------------
  // AwConfigChangeListener interface
  
  @Override
  public void onAwsDisabled() {
    if (elbClient.isSet()) {
      log.info("AWS integration has been disabled, shutting down AWS client");
      elbClient.get().shutdown();
      elbClient = OptionalValue.none();
    }
  }
  
  @Override
  public void onAwsEnabled() {
    if (elbClient.isNull()) {
      log.info("AWS integration has been enabled, creating AWS client");
      elbClient = OptionalValue.of(createElbClient());
    }
  }
  
  // --------------------------------------------------------------------------
  // ProcessPublishingProvider interface
  
  @Override
  public boolean accepts(ProcessPubConfig config) {
    return config instanceof AwsElbPublisherConfig && awsConf.isAwsEnabled();
  }

  @Override
  public void publish(ProcessPubContext context, PublishingCallback callback) {
    AwsElbPublisherConfig elbConf = (AwsElbPublisherConfig) context.getPubConfig();

    if (logger().isDebugEnabled()) {
      logger().debug(String.format("Registering process %s with elastic load-balancer %s", 
          ToStringUtil.toString(context.getProcess()), elbConf.getElbName()));
    }
    
    try {
      callback.publishingStarted(context);

      RegisterInstancesWithLoadBalancerRequest req = new RegisterInstancesWithLoadBalancerRequest(
          elbConf.getElbName(), 
          Arrays.asList(new Instance().withInstanceId(awsConf.getInstanceId()))
      );
      elbClient.get().registerInstancesWithLoadBalancer(req);
      callback.publishingSuccessful(context);
      waitForStatus(
        context, elbConf, 
        elbConf.getMaxPublishAttempts(), TimeUnit.SECONDS.toMillis(elbConf.getPublishInterval()), 
        new Condition<Set<String>>() {
          @Override
          public boolean apply(Set<String> registeredInstanceIds) {
            return !registeredInstanceIds.contains(awsConf.getInstanceId());
          }
        }
      );
      logger().debug(
          String.format("Completed registration of process %s with elastic load-balancer %s", 
          ToStringUtil.toString(context.getProcess()), elbConf.getElbName())
      );
      
    } catch (Exception e) {
      callback.publishingFailed(context, e);
    }
  }

  @Override
  public void unpublish(ProcessPubContext context, UnpublishingCallback callback) {
    AwsElbPublisherConfig elbConf = (AwsElbPublisherConfig) context.getPubConfig();
    if (logger().isDebugEnabled()) {
      logger().debug(String.format("Unregistering process %s from elastic load-balancer %s", 
          ToStringUtil.toString(context.getProcess()), elbConf.getElbName()));
    }
    callback.unpublishingStarted(context);
    try {
      DeregisterInstancesFromLoadBalancerRequest req = new DeregisterInstancesFromLoadBalancerRequest(
          elbConf.getElbName(), 
          Arrays.asList(new Instance().withInstanceId(awsConf.getInstanceId()))
      );
      elbClient.get().deregisterInstancesFromLoadBalancer(req);
      waitForStatus(
        context, elbConf, 
        elbConf.getMaxUnpublishAttempts(), TimeUnit.SECONDS.toMillis(elbConf.getUnpublishInterval()), 
        new Condition<Set<String>>() {
          @Override
          public boolean apply(Set<String> registeredInstanceIds) {
            return registeredInstanceIds.contains(awsConf.getInstanceId());
          }
        }
      );
      logger().debug(
          String.format("Completed unregistration of process %s from elastic load-balancer %s", 
          ToStringUtil.toString(context.getProcess()), elbConf.getElbName())
      );

      callback.unpublishingSuccessful(context);
    } catch (Exception e) {
      callback.unpublishingFailed(context, e);
    } 
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods

  // Visible for testing
  AmazonElasticLoadBalancing createElbClient() {
    return new AmazonElasticLoadBalancingClient(awsConf.getCredentials());
  }
  
  private void waitForStatus(ProcessPubContext ctx, AwsElbPublisherConfig elbConf, 
      int maxAttempts, long intervalMillis, Condition<Set<String>> shouldContinueCheck) 
      throws InterruptedException, IllegalStateException {
    boolean shouldContinue = true;
    int     attempts       = 0;
    do {
      if (log.isDebugEnabled())
        log.debug(String.format("Checking ELB %s state for process %s (attempt: %s)...", 
            elbConf.getElbName(), ToStringUtil.toString(ctx.getProcess()), (attempts + 1)));
      
      DescribeLoadBalancersResult result = elbClient.get().describeLoadBalancers(new DescribeLoadBalancersRequest(Arrays.asList(elbConf.getElbName())));
      Assertions.illegalState(result.getLoadBalancerDescriptions().isEmpty(), "No ELB found for: %s", elbConf.getElbName());
      LoadBalancerDescription desc = result.getLoadBalancerDescriptions().get(0);
      Set<String> instanceIds = Collects.convertAsSet(desc.getInstances(), new Func<String, Instance>() {
        @Override
        public String call(Instance instance) {
          return instance.getInstanceId();
        }
      });
      shouldContinue = shouldContinueCheck.apply(instanceIds);
      attempts++;
      if (shouldContinue && attempts < maxAttempts) {
        Thread.sleep(intervalMillis);
      }
    } while (shouldContinue && attempts < maxAttempts);
   
    Assertions.illegalState(
        shouldContinue, 
        "Publishing/unpublishing to/from ELB %s not completed within allocated attempts for process %s", 
        elbConf.getElbName(), ToStringUtil.toString(ctx.getProcess())
    );
  }
  

}
