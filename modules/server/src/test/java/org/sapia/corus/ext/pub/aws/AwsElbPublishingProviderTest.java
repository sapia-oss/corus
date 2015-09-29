package org.sapia.corus.ext.pub.aws;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.client.services.deployer.dist.AwsElbPublisherConfig;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

@RunWith(MockitoJUnitRunner.class)
public class AwsElbPublishingProviderTest {

  @Mock
  AwsConfiguration awsConf;
  
  @Mock
  private AmazonElasticLoadBalancing elbClient;
  
  @Mock
  private PublishingCallback pubCallback;
  
  @Mock
  private UnpublishingCallback unpubCallback;
  
  private AwsElbPublisherConfig pubConfig;
  
  private AwsElbPublishingProvider provider;
  
  private Process process;
  
  private Distribution dist;
  
  private ProcessConfig processConf;
  
  private ActivePort activePort;
  
  private ProcessPubContext context;
  
  @Before
  public void setUp() throws Exception {
    pubConfig = new AwsElbPublisherConfig();
    pubConfig.setElbName("test-elb");
    pubConfig.setMaxUnpublishAttempts(3);
    pubConfig.setUnpublishInterval(1);
    pubConfig.setMaxPublishAttempts(3);
    pubConfig.setPublishInterval(1);
    provider = new AwsElbPublishingProvider() {
      
      @Override
      AmazonElasticLoadBalancing createElbClient() {
        return elbClient;
      }
      
    };
    
    process     = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    dist = new Distribution("test", "1.0");
    processConf = new ProcessConfig("testProcess");
    activePort  = new ActivePort("test-port", 8080);
    context     = new ProcessPubContext(process, dist, processConf, activePort, pubConfig);
    
    when(awsConf.getInstanceId()).thenReturn("test-instance");
    when(awsConf.isAwsEnabled()).thenReturn(true);
    
    provider.setAwsConfiguration(awsConf);
    provider.init();
  }

  @Test
  public void testAccepts() {
    assertTrue(provider.accepts(pubConfig));
  }
  
  @Test
  public void testAccepts_aws_disabled() {
    Mockito.reset(awsConf);
    when(awsConf.isAwsEnabled()).thenReturn(false);
    assertFalse(provider.accepts(pubConfig));
  }

  @Test
  public void testPublish() {
    setUpElbClient(true);
    
    provider.publish(context, pubCallback);
    
    verify(pubCallback).publishingStarted(context);
    verify(pubCallback).publishingSuccessful(context);
  }

  @Test
  public void testUnpublish() {
    setUpElbClient(false);
    
    provider.unpublish(context, unpubCallback);
    
    verify(unpubCallback).unpublishingStarted(context);
    verify(unpubCallback).unpublishingSuccessful(context);
  }
  
  private void setUpElbClient(final boolean publish) {
    
    doAnswer(new Answer<DescribeLoadBalancersResult>() {
      int count = 0;
      @Override
      public DescribeLoadBalancersResult answer(InvocationOnMock invocation)
          throws Throwable {
        DescribeLoadBalancersResult result = new DescribeLoadBalancersResult();
        LoadBalancerDescription desc = new LoadBalancerDescription();
        desc.setLoadBalancerName(pubConfig.getElbName());
        result.setLoadBalancerDescriptions(Arrays.asList(desc));
        
        if (publish) {
          if (count >= 2) {
            desc.setInstances(Arrays.asList(new Instance().withInstanceId(awsConf.getInstanceId())));
          } else {
            desc.setInstances(new ArrayList<Instance>());
          }
        } else {
          if (count >= 2) {
            desc.setInstances(new ArrayList<Instance>());
          } else {
            desc.setInstances(Arrays.asList(new Instance().withInstanceId(awsConf.getInstanceId())));
          }
        }
        count++;
        return result;
      }
    }).when(elbClient).describeLoadBalancers(any(DescribeLoadBalancersRequest.class));
  }
  
}
