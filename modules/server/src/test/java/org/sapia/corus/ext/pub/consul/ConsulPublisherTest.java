package org.sapia.corus.ext.pub.consul;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.dist.ConsulPublisherConfig;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.HttpDiagnosticConfig;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.ext.pub.consul.ConsulPublisher.ConsulResponseFacade;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ConsulPublisherTest {
  
  @Mock
  private TaskManager          tasks;
  
  @Mock
  private InternalConfigurator configurator;
  
  @Mock
  private ConsulResponseFacade resp;
  
  @Mock
  private ServerContext        serverContext;
  
  @Mock
  private Corus                corus;

  @Mock 
  private TaskExecutionContext execContext;
  
  @Mock
  private PublishingCallback pubCallback;
  
  @Mock
  private UnpublishingCallback unpubCallback;
  
  private CorusHost            corusHost;
  
  private ConsulPublisher      publisher;
  
  @Captor
  private ArgumentCaptor<Task<Void, Void>> taskArg;

  @Captor
  private ArgumentCaptor<DynamicProperty<Boolean>> publishingEnabledArg;

  @Captor
  private ArgumentCaptor<DynamicProperty<Integer>> pingIntervalSecondsArg;
  
  @Captor
  private ArgumentCaptor<DynamicProperty<Integer>> pingTimeoutSecondsArg;
  
  @Before
  public void setUp() throws Exception {
    
    corusHost = CorusHost.newInstance("test-node", new Endpoint(new TCPAddress("test", "localhost", 33000), mock(ServerAddress.class)), "test", "test", mock(PublicKey.class));
    publisher = new ConsulPublisher() {
      @Override
      ConsulResponseFacade doInvokeUrl(URL url, String httpMethod) throws IOException {
        return resp;
      }
      
      @Override
      ConsulResponseFacade doSendPayload(URL url, String payloadContent,
          String httpMethod) throws IOException {
        return resp;
      }
    };
    
    publisher.setServerContext(serverContext);
    publisher.setConfigurator(configurator);
    publisher.setTasks(tasks);
    publisher.setPublishTtlSeconds(10);
    publisher.setPublishingEnabled(true);
    publisher.setPublishIntervalSeconds(5);
    publisher.setAgentUrl("http://localhost:8500");

    when(corus.getDomain()).thenReturn("test-domain");
    when(serverContext.getDomain()).thenReturn("test-domain");
    when(serverContext.getCorus()).thenReturn(corus);
    when(serverContext.getCorusHost()).thenReturn(corusHost);
    when(resp.getStatusCode()).thenReturn(200);
       
    publisher.init();
    
    verify(configurator).registerForPropertyChange(eq(CorusConsts.PROPERTY_CORUS_EXT_PUB_CONSUL_ENABLED), publishingEnabledArg.capture());

  }

  @Test
  public void testStart() throws Exception {
    publisher.start();
    assertTrue(publisher.isPublishTaskRunning());
    verify(tasks).executeBackground(taskArg.capture(), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testPublish_disabled() throws Throwable {
    publisher.setPublishingEnabled(false);
    publisher.start();
    assertFalse(publisher.isPublishTaskRunning());
    verify(tasks, never()).executeBackground(taskArg.capture(), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testStart_publish_disabled() throws Throwable {
    publisher.start();
    assertTrue(publisher.isPublishTaskRunning());
    verify(tasks).executeBackground(taskArg.capture(), any(Void.class), any(BackgroundTaskConfig.class));  
    
    publisher.setPublishingEnabled(false);
    taskArg.getValue().execute(execContext, null);
    assertFalse(publisher.isPublishTaskRunning());
  }
  
  @Test
  public void testAccepts() throws Exception {
    assertTrue(publisher.accepts(createPubContext().getPubConfig()));
  }

  @Test
  public void testAccepts_false() throws Exception {
    publisher.setEnabled(false);
    assertFalse(publisher.accepts(createPubContext().getPubConfig()));
  }
  
  @Test
  public void testPublishProcess_success() throws Exception {
    ProcessPubContext context = createPubContext();
    publisher.publish(context, pubCallback);
    
    verify(pubCallback).publishingStarted(context);
    verify(pubCallback).publishingSuccessful(context);
  }
  
  @Test
  public void testPublishProcess_failure() throws Exception {
    when(resp.getStatusCode()).thenReturn(500);

    ProcessPubContext context = createPubContext();
    publisher.publish(context, pubCallback);
    
    verify(pubCallback).publishingStarted(context);
    verify(pubCallback).publishingFailed(eq(context), any(Exception.class));
  }

  @Test
  public void testUnpublishProcess_success() throws Exception {
    ProcessPubContext context = createPubContext();
    publisher.unpublish(context, unpubCallback);
    
    verify(unpubCallback).unpublishingStarted(context);
    verify(unpubCallback).unpublishingSuccessful(context);
  }
  
  @Test
  public void testUnpublishProcess_failure() throws Exception {
    when(resp.getStatusCode()).thenReturn(500);

    ProcessPubContext context = createPubContext();
    publisher.unpublish(context, unpubCallback);
    
    verify(unpubCallback).unpublishingStarted(context);
    verify(unpubCallback).unpublishingFailed(eq(context), any(Exception.class));
  }
  
  private ProcessPubContext createPubContext() throws Exception{
    Process process = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    Distribution dist = new Distribution("test", "1.0");
    ProcessConfig processConf = new ProcessConfig("testProcess");
    Port portConf = processConf.createPort();
    portConf.setName("test-port");
    HttpDiagnosticConfig httpDiag = new HttpDiagnosticConfig();
    httpDiag.setPath("/ping");
    portConf.handleObject("diagConfig", httpDiag);
    
    ActivePort activePort = new ActivePort("test-port", 8080);
    ConsulPublisherConfig config = new ConsulPublisherConfig();
    return new ProcessPubContext(process, dist, processConf, activePort, config);
  }
}
