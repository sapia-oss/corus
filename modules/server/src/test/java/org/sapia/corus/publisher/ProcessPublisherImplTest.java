package org.sapia.corus.publisher;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.deployer.dist.PublishingConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.ubik.util.Collects;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class ProcessPublisherImplTest {
  
  @Mock
  private ProcessPublishingProvider p1, p2;
  
  @Mock
  private ProcessPubConfig pubConf;
  
  @Mock
  private Deployer deployer;
 
  @Mock
  private Map<String, ProcessPublishingProvider> providers;
  
  @Mock
  private ApplicationContext appCtx;
  
  @Mock
  private PublishingCallback pubCallback;
  
  @Mock
  private UnpublishingCallback unpubCallback;
  
  private Distribution dist;
  
  private Process withPorts, withoutPorts;
  
  private ProcessPublisherImpl publisher;
  

  @Before
  public void setUp() throws Exception {
    dist = new Distribution("test", "1.0");
    ProcessConfig pc = new ProcessConfig("testProcess");
    
    Port port = pc.createPort();
    port.setName("test-port");
    PublishingConfig publishing = port.createPublishing();
    publishing.getConfigs().add(pubConf);
    
    dist.addProcess(pc);
    
    publisher = new ProcessPublisherImpl();
    withoutPorts = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    withPorts = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    withPorts.addActivePort(new ActivePort("test-port", 8080));
    
    when(p1.accepts(any(ProcessPubConfig.class))).thenReturn(false);
    when(p2.accepts(pubConf)).thenReturn(true);
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ProcessPubContext  ctx = invocation.getArgumentAt(0, ProcessPubContext.class);
        PublishingCallback cb  = invocation.getArgumentAt(1, PublishingCallback.class);
        cb.publishingStarted(ctx);
        cb.publishingSuccessful(ctx);
        return null;
      }
    }).when(p2).publish(any(ProcessPubContext.class), eq(pubCallback));
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        ProcessPubContext    ctx = invocation.getArgumentAt(0, ProcessPubContext.class);
        UnpublishingCallback cb  = invocation.getArgumentAt(1, UnpublishingCallback.class);
        cb.unpublishingStarted(ctx);
        cb.unpublishingSuccessful(ctx);
        return null;
      }
    }).when(p2).unpublish(any(ProcessPubContext.class), eq(unpubCallback));
    
    when(providers.values()).thenReturn(Arrays.asList(p1, p2));
    when(appCtx.getBeansOfType(ProcessPublishingProvider.class)).thenReturn(providers);
    
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenReturn(dist);
    
    publisher.setApplicationContext(appCtx);
    publisher.setDeployer(deployer);
    publisher.init();
  }

  @Test
  public void testPublishProcess_no_active_port() {
    publisher.publishProcess(withoutPorts, pubCallback);
    
    verify(pubCallback).publishingNotApplicable(withoutPorts);
  }
  
  @Test
  public void testPublishProcess_no_provider() {
    publisher.setPublishers(Collects.arrayToList(p1));
    
    publisher.publishProcess(withPorts, pubCallback);
    
    verify(pubCallback).publishingNotApplicable(withPorts);
  }
  
  @Test
  public void testPublishProcess() {
    publisher.publishProcess(withPorts, pubCallback);
    
    verify(pubCallback).publishingStarted(any(ProcessPubContext.class));
    verify(pubCallback).publishingSuccessful(any(ProcessPubContext.class));
  }

  @Test
  public void testUnpublishProcess_no_active_port() {
    publisher.unpublishProcess(withoutPorts, unpubCallback);
    
    verify(unpubCallback).unpublishingNotApplicable(withoutPorts);
  }

  @Test
  public void testUnpublishProcess_no_provider() {
    publisher.setPublishers(Collects.arrayToList(p1));
    
    publisher.unpublishProcess(withPorts, unpubCallback);
    
    verify(unpubCallback).unpublishingNotApplicable(withPorts);
  }
  
  @Test
  public void testUnpublishProcess() {
    publisher.unpublishProcess(withPorts, unpubCallback);
    
    verify(unpubCallback).unpublishingStarted(any(ProcessPubContext.class));
    verify(unpubCallback).unpublishingSuccessful(any(ProcessPubContext.class));
  }

}
