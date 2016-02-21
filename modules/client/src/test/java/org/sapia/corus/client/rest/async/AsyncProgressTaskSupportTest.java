package org.sapia.corus.client.rest.async;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport.AsyncProgressTaskContext;
import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;

@RunWith(MockitoJUnitRunner.class)
public class AsyncProgressTaskSupportTest {

  private static final int MIN_HOSTS  = 2;
  private static final int BATCH_SIZE = 2;
  private static final TimeValue TIMEOUT = TimeValue.createSeconds(30);
  
  @Mock
  private AsyncProgressTaskContext context;
  
  @Mock
  private Func<ProgressResult, ClusterInfo> callback;
  
  @Mock
  private CorusConnector connector;
 
  @Mock
  private ConnectorPool  connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  private MutableClock clock;
  
  private AsyncProgressTaskSupport task;
  
  @Before
  public void setUp() throws Exception {
    
    context = new AsyncProgressTaskContext()
      .minHosts(MIN_HOSTS)
      .batchSize(BATCH_SIZE)
      .connectors(connectors)
      .timeout(TIMEOUT);
   
    clock = MutableClock.getInstance();
    
    task = new AsyncProgressTaskSupport(context, clock) {

      @Override
      protected void doExecute() {
        try {
          process();
        } catch (Exception e) {
          throw new IllegalStateException("Error caught", e);
        }
      }
      
      @Override
      protected ProgressResult doProcess(AsyncParams params) {
        return callback.call(params.getClusterInfo());
      }
    };
 
    when(connectors.acquire()).thenReturn(connector);
    when(connector.getContext()).thenReturn(connection);
  }

  @Test
  public void testProcess_multiple_batches() throws Exception {
    when(connection.getServerHost()).thenReturn(createHost());
    when(connection.getOtherHosts()).thenReturn(createHosts(5));
    setUpExpectedResults(3);
    
    task.execute();
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_OK, r.getStatus());
    assertEquals(2, r.getProcessedHosts().size());
  }
  
  @Test
  public void testProcess_multiple_batches_with_max_errors_partial_success() throws Exception {
    context.maxErrors(2);
    when(connection.getServerHost()).thenReturn(createHost());
    when(connection.getOtherHosts()).thenReturn(createHosts(9));
    setUpErrorResults(3, 5);
    
    task.execute();
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR, task.getNextResult().getStatus());
    
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_PARTIAL_SUCCESS, r.getStatus());
    assertEquals(2, r.getProcessedHosts().size());
  }
  
  @Test
  public void testProcess_multiple_batches_with_max_errors_within_threshold_partial_success() throws Exception {
    context.maxErrors(2);
    when(connection.getServerHost()).thenReturn(createHost());
    when(connection.getOtherHosts()).thenReturn(createHosts(9));
    setUpErrorResults(2, 5);
    
    task.execute();
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, task.getNextResult().getStatus());
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR, task.getNextResult().getStatus());
    
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_PARTIAL_SUCCESS, r.getStatus());
    assertEquals(2, r.getProcessedHosts().size());
  }
  
  @Test
  public void testProcess_min_hosts() throws Exception {
    when(connection.getServerHost()).thenReturn(createHost());
    setUpExpectedResults(1);
    
    task.execute();
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_OK, r.getStatus());
    assertEquals(1, r.getProcessedHosts().size());
  }
  
  @Test
  public void testProcess_terminated() throws Exception {
    when(connection.getServerHost()).thenReturn(createHost());
    setUpExpectedResults(1);
    
    task.terminate();
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_PARTIAL_CONTENT, r.getStatus());
  }
  
  @Test
  public void testProcess_error() throws Exception {
    when(connection.getServerHost()).thenReturn(createHost());
    when(connection.getOtherHosts()).thenReturn(createHosts(5));
    when(callback.call(any(ClusterInfo.class))).thenReturn(
        new ProgressResult(Arrays.asList("msg1")).error()
    );
    task.execute();
    ProgressResult r = task.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
    assertEquals(2, r.getProcessedHosts().size());
  }
  
  
  @Test(expected = TimeoutException.class)
  public void testProcess_timeout() throws Exception {
    when(connection.getServerHost()).thenReturn(createHost());
    when(connection.getOtherHosts()).thenReturn(createHosts(5));
    clock.increaseCurrentTimeMillis(31000);
    
    ProgressResult r = task.getNextResult();
    
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
    assertEquals(2, r.getProcessedHosts().size());
  }
  
  @Test
  public void testAssignErrorStatus_no_max_errors_error_in_first_batch() {
    ProgressResult r = new ProgressResult(Arrays.asList("error")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    // max errors: 0
    // errors thus far: 1
    // number of batches 1
    // current batch index: 0 (meaning 1 batch has been processed thus far).
    AsyncProgressTaskSupport.assignErrorStatus(r, 0, 1, 1, 0);
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
  }
  
  @Test
  public void testAssignErrorStatus_no_max_errors_error_in_second_batch() {
    ProgressResult r = new ProgressResult(Arrays.asList("error")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    // max errors: 0
    // errors thus far: 1
    // number of batches 2
    // current batch index: 1 (meaning 2 batches have been processed thus far).
    AsyncProgressTaskSupport.assignErrorStatus(r, 0, 1, 2, 1);
    assertEquals(HttpResponseFacade.STATUS_PARTIAL_SUCCESS, r.getStatus());
  }
  
  @Test
  public void testAssignErrorStatus_with_max_errors_and_all_batches_in_error() {
    ProgressResult r = new ProgressResult(Arrays.asList("error")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    // max errors: 3
    // errors thus far: 3
    // number of batches 3
    // current batch index: 2 (meaning 3 batches have been processed thus far).
    AsyncProgressTaskSupport.assignErrorStatus(r, 3, 3, 3, 2);
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
  }
  
  @Test
  public void testAssignErrorStatus_with_max_errors_and_some_batches_in_error() {
    ProgressResult r = new ProgressResult(Arrays.asList("error")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    // max errors: 2
    // errors thus far: 3
    // number of batches 4
    // current batch index: 3 (meaning 4 batches have been processed thus far).
    AsyncProgressTaskSupport.assignErrorStatus(r, 2, 3, 4, 3);
    assertEquals(HttpResponseFacade.STATUS_PARTIAL_SUCCESS, r.getStatus());
  }
  
  @Test
  public void testAssignErrorStatus_with_max_errors_and_some_batches_in_error_and_batches_remaining() {
    ProgressResult r = new ProgressResult(Arrays.asList("error")).setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
    // max errors: 2
    // errors thus far: 3
    // number of batches 5
    // current batch index: 3 (meaning 4 batches have been processed thus far).
    AsyncProgressTaskSupport.assignErrorStatus(r, 2, 3, 5, 3);
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS_ERROR, r.getStatus());
  }


  private void setUpExpectedResults(final int numExpected) {
    doAnswer(new Answer<ProgressResult>() {
      int count = 0;
      @Override
      public ProgressResult answer(InvocationOnMock invocation)
          throws Throwable {
        ProgressResult r = new ProgressResult(Arrays.asList("msg1", "msg2"));
        if (count == numExpected - 1) {
          r.setStatus(HttpResponseFacade.STATUS_OK);
        } else {
          r.setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
        }
        count++;
        return r;
      }
    }).when(callback).call(any(ClusterInfo.class));
  }
  
  private void setUpErrorResults(final int numErrors, final int totalResults) {
    doAnswer(new Answer<ProgressResult>() {
      int count = 0;
      int successes = totalResults - numErrors;
      @Override
      public ProgressResult answer(InvocationOnMock invocation)
          throws Throwable {
        ProgressResult r = new ProgressResult(Arrays.asList("msg1", "msg2"));
        if (count < successes) {
          r.setStatus(HttpResponseFacade.STATUS_OK);
        } else {
          r.setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
        }
        count++;
        return r;
      }
    }).when(callback).call(any(ClusterInfo.class));
  }
  
  private List<CorusHost> createHosts(int quantity) {
    List<CorusHost> hosts = new ArrayList<CorusHost>();
    for (int i = 0; i < quantity; i++) {
      hosts.add(createHost());
    }
    return hosts;
  }
  
  private CorusHost createHost() {
    return CorusHost.newInstance(
        new Endpoint(Mockito.mock(ServerAddress.class), Mockito.mock(ServerAddress.class)), "testOs", "testJvm",
        mock(PublicKey.class)
    );
  }
}
