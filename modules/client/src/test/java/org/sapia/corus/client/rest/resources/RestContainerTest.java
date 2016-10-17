package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.RestContainer;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.rest.RestContainer.Builder;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class RestContainerTest {
  
  @Mock
  private RestRequest request;
  
  @Mock
  private RestResponseFacade response;
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private CorusConnectionContext connectionContext;
  
  @Mock
  private ServerAddress address;
  
  @Mock
  private Auditor auditor;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  private RestContainer container;
  private TestResource resource;

  @Before
  public void setUp() {
    resource  = new TestResource();
    container = RestContainer.Builder.newInstance().auditor(auditor).resource(resource).build();
    when(request.getAccepts()).thenReturn(Collects.arrayToSet(ContentTypes.APPLICATION_JSON));
    when(request.getMethod()).thenReturn(HttpMethod.GET);
    when(connector.getContext()).thenReturn(connectionContext);
    when(connectionContext.getServerHost()).thenReturn(
        CorusHost.newInstance(
            "test-node", 
            new Endpoint(address, address), "os", "jvm", 
            Encryption.generateDefaultKeyPair().getPublic()
        )
     );
  }
  
  @Test
  public void testInvoke() throws Throwable {
    when(request.getPath()).thenReturn("/test/method1");
    container.invoke(new RequestContext(request, connector, async, partitions, connectors), response);
    assertTrue(resource.method1Invoked);
    
    when(request.getPath()).thenReturn("/test/method2");
    container.invoke(new RequestContext(request, connector, async, partitions, connectors), response);
    assertTrue(resource.method2Invoked);
  }

  
  public static class TestResource {
    
    private boolean method1Invoked, method2Invoked;
    
    @Path("/test/method1")
    @HttpMethod(HttpMethod.GET)
    @Output(ContentTypes.APPLICATION_JSON)
    public String invokeMethod1(RequestContext request) {
      method1Invoked = true;
      return "method1";
    }
    
    @Path("/test/method1")
    @HttpMethod(HttpMethod.POST)
    @Output(ContentTypes.APPLICATION_JSON)
    public String invokeMethod1_port(RequestContext request) {
      return "method1";
    }
    
    @Path("/test/method1")
    @HttpMethod(HttpMethod.POST)
    @Output(ContentTypes.TEXT_HTML)
    public String invokeMethod1_html(RequestContext request) {
      return "method1";
    }
    
    @Path("/test/method2")
    @HttpMethod(HttpMethod.GET)
    @Output(ContentTypes.APPLICATION_JSON)
    public String invokeMethod2(RequestContext request) {
      method2Invoked = true;
      return "method2";
    }
    
  }
}
