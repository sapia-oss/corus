package org.sapia.corus.client.rest.async;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Result.Type;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.rest.ProgressResult;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.TimeValue;
import org.sapia.corus.client.services.processor.Process;

@RunWith(MockitoJUnitRunner.class)
public class WaitForProcessesKilledTaskTest {

  @Mock
  private CorusConnector connector;
  
  @Mock
  private ProcessorFacade processor;
  
  private WaitForProcessesKilledTask task;
  
  
  @Before
  public void setUp() throws Exception {
    when(connector.getProcessorFacade()).thenReturn(processor);
    
    task = new WaitForProcessesKilledTask(ProcessCriteria.builder().all(), new AsyncParams(connector, ClusterInfo.clustered()));
    task.setRetryInterval(TimeValue.createMillis(100));
    task.setTimeout(TimeValue.createMillis(300));
  }

  @Test
  public void testDoExecute_not_killed() {
    Results<List<Process>> results = new Results<List<Process>>();
    Process process = new Process(new DistributionInfo("dist", "1.0", "test", "proc"), "1234");
  
    Result<List<Process>> result = new Result<List<Process>>(
        CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "os", "jvm", mock(PublicKey.class)),
        Arrays.asList(process), Type.COLLECTION
    );
    results.addResult(result);
    
    when(processor.getProcesses(any(ProcessCriteria.class), any(ClusterInfo.class))).thenReturn(results);
    
    task.doExecute();
    ProgressResult progress = task.drainAllResults();
    
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, progress.getStatus());
  }
  
  @Test
  public void testDoExecute_killed() {
    Results<List<Process>> results = new Results<List<Process>>();
  
    Result<List<Process>> result = new Result<List<Process>>(
        CorusHost.newInstance(new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "os", "jvm", mock(PublicKey.class)),
        new ArrayList<Process>(), Type.COLLECTION
    );
    results.addResult(result);
    
    when(processor.getProcesses(any(ProcessCriteria.class), any(ClusterInfo.class))).thenReturn(results);
    
    task.doExecute();
    ProgressResult progress = task.drainAllResults();
    
    assertEquals(HttpResponseFacade.STATUS_OK, progress.getStatus());
  }

 
}
