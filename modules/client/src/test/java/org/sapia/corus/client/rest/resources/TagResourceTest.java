package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.TagResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class TagResourceTest {
  
  @Mock
  private CorusConnector           connector;
  
  @Mock
  private ConnectorPool            connectors;
  
  @Mock
  private CorusConnectionContext   connection;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest              request;
  
  @Mock
  private ConfiguratorFacade       confs;
  
  private TagResource              resource;
  private Results<Set<Tag>> results;
  
  @Before
  public void setUp() throws Exception {
    resource = new TagResource();
    
    results = new Results<Set<Tag>>();
    int count = 0;
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      Set<Tag> tags = new HashSet<Tag>();
      for (int j = 0; j < 5; j++) {
        tags.add(new Tag("tag-" + count));
        count++;
      }
      Result<Set<Tag>> result = new Result<Set<Tag>>(host, tags, Result.Type.COLLECTION);
      results.addResult(result);
    }
    
    when(connectors.acquire()).thenReturn(connector);
    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connector.getConfigFacade()).thenReturn(confs);
    when(confs.getTags(any(ClusterInfo.class))).thenReturn(results);
    when(connector.getContext()).thenReturn(connection);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("corus:scope")).thenReturn(new Value("corus:scope", "process"));
    when(request.getValue("t", "*")).thenReturn(new Value("t", "*"));
  }

  @Test
  public void testGetTagsForCluster() {
    String response = resource.getTagsForCluster(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONArray tags = json.getJSONObject(i).getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        doCheckTag(tags.getString(j), count++);
      }
    }
  }

  @Test
  public void testGetTagsForHost() {
    String response = resource.getTagsForHost(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONArray tags = json.getJSONObject(i).getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        doCheckTag(tags.getString(j), count++);
      }
    }
  }
  
  private void doCheckTag(String value, int i) {
    assertEquals("tag-" + i, value);
  }  

}
