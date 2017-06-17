package org.sapia.corus.client.rest.async;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Result.Type;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.DiagnosticFacade;
import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProgressDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticResult;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.TimeValue;

@RunWith(MockitoJUnitRunner.class)
public class PartialDiagnosticTaskTest {
  
  @Mock
  private CorusConnector        connector;
  
  @Mock
  private DiagnosticFacade      facade;
  
  private ClusterInfo           cluster;
  
  private AsyncParams           params;
  
  private PartialDiagnosticTask task;

  @Before
  public void setUp() throws Exception {
    cluster = ClusterInfo.clustered();
    params  = new AsyncParams(connector, cluster);
    task    = new PartialDiagnosticTask(params);
    task.setRetryInterval(TimeValue.createMillis(100));
    when(connector.getDiagnosticFacade()).thenReturn(facade);
  }

  @Test
  public void testDoTerminate() {
  }

  @Test
  public void testDoExecute_success() throws Exception {
    setupDiagnosticResult(0, 4);
    task.execute();
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_OK, r.getStatus());
  }
  
  @Test
  public void testDoExecute_failure() throws Exception {
    setupDiagnosticResult(2, 2);
    task.execute();
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
  }
  

  @Test
  public void testReleaseResources() {
  }
  
  private void setupDiagnosticResult(int numError, int numSuccess) {
    final List<Result<GlobalDiagnosticResult>> finalResults   = new ArrayList<>();
    final List<Result<GlobalDiagnosticResult>> pendingResults = new ArrayList<>();
            
    for (int i = 0; i < numError; i++) {
      GlobalDiagnosticResult gdr = new GlobalDiagnosticResult(
          GlobalDiagnosticStatus.FAILURE, 
          new ArrayList<SystemDiagnosticResult>(),
          new ArrayList<ProcessConfigDiagnosticResult>(), 
          OptionalValue.of(new ProgressDiagnosticResult(Arrays.asList("error")))
      );
      Result<GlobalDiagnosticResult> r = new Result<GlobalDiagnosticResult>(corusHost(i), gdr, Type.ELEMENT);
      finalResults.add(r);
    }
    
    for (int i = 0; i < (numError + numSuccess); i++) {
      GlobalDiagnosticResult gdr = new GlobalDiagnosticResult(
          GlobalDiagnosticStatus.INCOMPLETE, 
          new ArrayList<SystemDiagnosticResult>(),
          new ArrayList<ProcessConfigDiagnosticResult>(), 
          OptionalValue.none()
      );
      Result<GlobalDiagnosticResult> r = new Result<GlobalDiagnosticResult>(corusHost(i), gdr, Type.ELEMENT);
      pendingResults.add(r);
    }
    
    for (int i = 0; i < numSuccess; i++) {
      GlobalDiagnosticResult gdr = new GlobalDiagnosticResult(
          GlobalDiagnosticStatus.SUCCESS, 
          new ArrayList<SystemDiagnosticResult>(),
          new ArrayList<ProcessConfigDiagnosticResult>(), 
          OptionalValue.none()
      );
      Result<GlobalDiagnosticResult> r = new Result<GlobalDiagnosticResult>(corusHost(i), gdr, Type.ELEMENT);
      finalResults.add(r);
    }   
    
    doAnswer(new Answer<Results<GlobalDiagnosticResult>>() {
      int attempts = 0;
      @Override
      public Results<GlobalDiagnosticResult> answer(InvocationOnMock invocation) throws Throwable {
        attempts++;
        Results<GlobalDiagnosticResult> toReturn = new Results<GlobalDiagnosticResult>();
        if (attempts == 3) {
          toReturn.addResults(finalResults);
          return toReturn;
        } else {
          toReturn.addResults(pendingResults);
          return toReturn;
        }
      }
    }).when(facade).acquireDiagnostics(cluster);
  }
  

  private CorusHost corusHost(int i) {
    return CorusHost.newInstance("test-node", new Endpoint(new TCPAddress("test", "host-" + i, i), new TCPAddress("test", "host-" + i, i)), "os", "jvm", mock(PublicKey.class));
  }

}
