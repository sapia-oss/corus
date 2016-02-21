package org.sapia.corus.client.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.http.HttpStatus;
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
import org.sapia.corus.client.facade.DiagnosticFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.DiagnosticResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticEnv;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProgressDiagnosticResult;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.TimeValue;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosticResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService  async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest request;
  
  @Mock
  private RestResponseFacade response;
  
  @Mock
  private DiagnosticFacade diagnosticfacade;
  
  @Mock
  private ProcessConfigDiagnosticEnv env;
  
  private DiagnosticResource              resource;
  private Results<GlobalDiagnosticResult> results;
  private RequestContext                  context;

  private Distribution                    dist;
  private ProcessConfig                   processConf;
  private Process                         process;
  
  @Before
  public void setUp() throws Exception {
    resource = new DiagnosticResource();
    results = new Results<GlobalDiagnosticResult>();
    context = new RequestContext(request, connector, async, partitions, connectors);
    dist = new Distribution("test", "1.0");
    processConf = new ProcessConfig("testProcess");
    process =  new Process(new DistributionInfo("test", "1.0", "testProfile", "testProcess"));
    
    when(connectors.acquire()).thenReturn(connector);
    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connector.getDiagnosticFacade()).thenReturn(diagnosticfacade);
    when(diagnosticfacade.acquireDiagnostics(any(ClusterInfo.class)))
      .thenReturn(results);
    when(connector.getContext()).thenReturn(connection);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue(eq("contentLevel"), anyString())).thenReturn(new Value("contentLevel", "summary"));
    
    when(env.getDistribution()).thenReturn(dist);
    when(env.getExpectedInstanceCount()).thenReturn(1);
    when(env.getGracePeriod()).thenReturn(TimeValue.createMillis(1000));
    when(env.getProcessConfig()).thenReturn(processConf);
    when(env.getProcesses()).thenReturn(Arrays.asList(process));
  }

  @Test
  public void testGetDiagnosticsForCluster_success() {
    for (int i = 0; i < 5; i++) {
      results.addResult(createHostResult(i, createProcessConfigDiagnostics(5, 0, 0), createProgressDiagnostics(false)));
    }
    
    String json = resource.getDiagnosticsForCluster(context, response);
    JSONArray.fromObject(json);
    
    verify(response, never()).setStatus(anyInt());
    verify(response, never()).setStatusMessage(anyString());
  }
  
  @Test
  public void testGetDiagnosticsForCluster_process_pending() {
    for (int i = 0; i < 5; i++) {
      results.addResult(createHostResult(i, createProcessConfigDiagnostics(5, 0, 0), createProgressDiagnostics(false)));
    }
    results.addResult(createHostResult(5, createProcessConfigDiagnostics(0, 1, 0), createProgressDiagnostics(false)));
    
    JSONArray.fromObject(resource.getDiagnosticsForCluster(context, response));
    
    verify(response).setStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
    verify(response).setStatusMessage(anyString());
  }
  
  @Test
  public void testGetDiagnosticsForCluster_process_error() {
    for (int i = 0; i < 5; i++) {
      results.addResult(createHostResult(i, createProcessConfigDiagnostics(5, 0, 0), createProgressDiagnostics(false)));
    }
    results.addResult(createHostResult(5, createProcessConfigDiagnostics(0, 0, 1), createProgressDiagnostics(false)));
    
    JSONArray.fromObject(resource.getDiagnosticsForCluster(context, response));
    
    verify(response).setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    verify(response).setStatusMessage(anyString());
  }
  
  @Test
  public void testGetDiagnosticsForCluster_progress_error() {
    for (int i = 0; i < 5; i++) {
      results.addResult(createHostResult(i, createProcessConfigDiagnostics(5, 0, 0), createProgressDiagnostics(false)));
    }
    results.addResult(createHostResult(5, createProcessConfigDiagnostics(0, 0, 0), createProgressDiagnostics(true)));
    
    JSONArray.fromObject(resource.getDiagnosticsForCluster(context, response));
    
    verify(response).setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    verify(response).setStatusMessage(anyString());
  }
  
  private Result<GlobalDiagnosticResult> createHostResult(int index, List<ProcessConfigDiagnosticResult> processResults, ProgressDiagnosticResult progressResult) {
    CorusHost host = CorusHost.newInstance(
        new Endpoint(new TCPAddress("test", "host-" + index, index), mock(ServerAddress.class)), 
        "os-" + index, 
        "jvm-" + index,
        mock(PublicKey.class)
    );
    
    return new Result<GlobalDiagnosticResult>(host,
        GlobalDiagnosticResult.Builder.newInstance()
          .processDiagnostics(processResults)
          .progressDiagnostics(progressResult)
          .build(),
        Result.Type.ELEMENT
    );
    
  }
  
  private ProgressDiagnosticResult createProgressDiagnostics(boolean error) {
    
    if (error) {
      return new ProgressDiagnosticResult(Arrays.asList("error occurred"));
    } else {
      return new ProgressDiagnosticResult(new ArrayList<String>());
    }
    
  }
  
  private List<ProcessConfigDiagnosticResult> createProcessConfigDiagnostics(int successCount, int pendingCount, int errorCount) {
    List<ProcessConfigDiagnosticResult> toReturn = new ArrayList<ProcessConfigDiagnosticResult>();
    
    for (int i = 0; i < successCount; i++) {
      ActivePort port = new ActivePort("testPort", 8000);
      ProcessConfigDiagnosticResult r = ProcessConfigDiagnosticResult.Builder.newInstance()
        .distribution(new Distribution("test", "1.0"))
        .processConfig(new ProcessConfig("testProcess"))
        .results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "success", process, "http", port))
        .build(env);
      
      toReturn.add(r);
    }
    
    for (int i = 0; i < errorCount; i++) {
      ActivePort port = new ActivePort("testPort", 8000);
      ProcessConfigDiagnosticResult r = ProcessConfigDiagnosticResult.Builder.newInstance()
        .distribution(new Distribution("test", "1.0"))
        .processConfig(new ProcessConfig("testProcess"))
        .results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_FAILED, "success", process, "http", port))
        .build(env);
      
      toReturn.add(r);
    }
    
    for (int i = 0; i < pendingCount; i++) {
      ProcessConfigDiagnosticResult r = ProcessConfigDiagnosticResult.Builder.newInstance()
        .distribution(new Distribution("test", "1.0"))
        .processConfig(new ProcessConfig("testProcess"))
        .results(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "locked", process))
        .build(env);
      
      toReturn.add(r);
    }
    
    return toReturn;
  }

}
