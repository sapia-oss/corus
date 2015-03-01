package org.sapia.corus.ext.pub.consul;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

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
    
    corusHost = CorusHost.newInstance(new Endpoint(new TCPAddress("test", "localhost", 33000), mock(ServerAddress.class)), "test", "test");
    publisher = new ConsulPublisher() {
      @Override
      ConsulResponseFacade doInvokeUrl(URL url) throws IOException {
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
  
}
