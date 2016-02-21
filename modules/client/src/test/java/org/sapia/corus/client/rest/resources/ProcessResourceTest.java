package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.ProcessResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ProcessResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest    request;
  
  @Mock
  private ProcessorFacade processor;
  
  private ProcessResource        resource;
  private Results<List<Process>> results;
  
  @Before
  public void setUp() {
    resource = new ProcessResource();
    
    results = new Results<List<Process>>();
    int processCount = 0;
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      List<Process> processes = new ArrayList<Process>();
      for (int j = 0; j < 5; j++) {
        DistributionInfo dist = new DistributionInfo("dist", "1.0", "test-profile", "test-process");
        Process p = new Process(dist, String.format("id-%s", processCount));
        p.setOsPid(String.format("pid-%s", processCount));
        p.setStatus( LifeCycleStatus.ACTIVE);
        processes.add(p);
        processCount++;
      }
      Result<List<Process>> result = new Result<List<Process>>(host, processes, Result.Type.COLLECTION);
      results.addResult(result);
    }
    
    when(connectors.acquire()).thenReturn(connector);
    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connector.getProcessorFacade()).thenReturn(processor);
    when(processor.getProcesses(any(ProcessCriteria.class), any(ClusterInfo.class)))
      .thenReturn(results);
    when(connector.getContext()).thenReturn(connection);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("p")).thenReturn(new Value("p", "test-profile"));
    when(request.getValue("pr")).thenReturn(new Value("pr", "*:*"));
    when(request.getValue(anyString(), anyString())).thenReturn(new Value("test", "*"));
  }

  @Test
  public void testGetProcessesForCluster() {
    String response = resource.getProcessesForCluster(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject p = json.getJSONObject(i).getJSONObject("data");
      doCheckProcess(p, count++);
    }
  }

  @Test
  public void testGetProcessesForHost() {
    String response = resource.getProcessesForHost(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject p = json.getJSONObject(i).getJSONObject("data");
      doCheckProcess(p, count++);
    }
  }

  @Test
  public void testGetProcess() {
    when(request.getValue("corus:process_id")).thenReturn(new Value("corus:process_id", "" + 0));
    results = new Results<List<Process>>();
    CorusHost host = CorusHost.newInstance(
        new Endpoint(new TCPAddress("test", "host", 0), mock(ServerAddress.class)), 
        "os", 
        "jvm",
        mock(PublicKey.class)
    );
    host.setHostName("hostname");
    host.setRepoRole(RepoRole.CLIENT);
    List<Process> processes = new ArrayList<Process>();
    DistributionInfo dist = new DistributionInfo("dist", "1.0", "test-profile", "test-process");
    Process proc = new Process(dist, String.format("id-0"));
    proc.setOsPid(String.format("pid-0"));
    proc.setStatus( LifeCycleStatus.ACTIVE);
    processes.add(proc);
    Result<List<Process>> result = new Result<List<Process>>(host, processes, Result.Type.COLLECTION);
    results.addResult(result);
    
    when(processor.getProcesses(any(ProcessCriteria.class), any(ClusterInfo.class))).thenReturn(results);
    
    String response = resource.getProcess(new RequestContext(request, connector, async, partitions, connectors));
    JSONObject json = JSONObject.fromObject(response);
    JSONObject p = json.getJSONObject("data");
    doCheckProcess(p, 0);
  }
  
  private void doCheckProcess(JSONObject process, int i) {
    assertEquals("1.0", process.getString("version"));
    assertEquals("dist", process.getString("distribution"));
    assertEquals("test-process", process.getString("name"));
    assertEquals("test-profile", process.getString("profile"));
    assertEquals(process.getString("pid"), "pid-" + i);
    assertEquals(process.getString("id"), "id-" + i);
    assertEquals(LifeCycleStatus.ACTIVE.name(), process.getString("status"));
  }  

}
