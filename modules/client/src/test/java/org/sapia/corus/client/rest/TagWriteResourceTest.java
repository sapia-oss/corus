package org.sapia.corus.client.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class TagWriteResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private RestRequest          request;
  
  @Mock
  private ConfiguratorFacade   conf;
  
  private RequestContext       context;
  
  private TagWriteResource     resource;
  
  @Before
  public void setUp() {
    resource = new TagWriteResource();
    context  = new RequestContext(request, connector);
    
    when(connector.getContext()).thenReturn(connection);
    when(connector.getConfigFacade()).thenReturn(conf);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("corus:tag")).thenReturn(new Value("corus:tag", "test"));
  }

  @Test
  public void testAddTagsForCluster() {
    resource.addTagsForCluster(context);
    verify(conf).addTags(argThat(tagsMatcher(Collects.arrayToSet("test"))), any(ClusterInfo.class));
  }

  @Test
  public void testAddTagsForHost() {
    resource.addTagsForCluster(context);
    verify(conf).addTags(argThat(tagsMatcher(Collects.arrayToSet("test"))), any(ClusterInfo.class));
  }

  @Test
  public void testDeleteTagForCluster() {
    resource.deleteTagForCluster(context);
    verify(conf).removeTag(eq("test"), any(ClusterInfo.class));
  }

  @Test
  public void testDeleteTagForHost() {
    resource.deleteTagForHost(context);
    verify(conf).removeTag(eq("test"), any(ClusterInfo.class));
  }
  
  private ArgumentMatcher<Set<String>> tagsMatcher(final Set<String> expected) {
    return new ArgumentMatcher<Set<String>>() {
      @SuppressWarnings("unchecked")
      @Override
      public boolean matches(Object argument) {
        Set<String> actual = (Set<String>) argument;
        return actual.containsAll(expected);
      }
    };
  }

}
