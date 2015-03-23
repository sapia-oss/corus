package org.sapia.corus.client.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcessCriteria;

@RunWith(MockitoJUnitRunner.class)
public class ProcessWriteResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private RestRequest          request;
  
  @Mock
  private ProcessorFacade      processor;
  
  private RequestContext       context;
  
  private ProcessWriteResource resource;
  
  @Before
  public void setUp() {
    resource = new ProcessWriteResource();
    context  = new RequestContext(request, connector);
    
    when(connector.getContext()).thenReturn(connection);
    when(connector.getProcessorFacade()).thenReturn(processor);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    
    when(request.getValue("p")).thenReturn(new Value("p", "test-profile"));
    when(request.getValue("d")).thenReturn(new Value("d", "test-dist"));
    when(request.getValue("v")).thenReturn(new Value("v", "test-version"));
    when(request.getValue("n")).thenReturn(new Value("n", "test-name"));
    
    when(request.getValue("p", "*")).thenReturn(new Value("p", "*"));
    when(request.getValue("d", "*")).thenReturn(new Value("d", "*"));
    when(request.getValue("v", "*")).thenReturn(new Value("v", "*"));
    when(request.getValue("n", "*")).thenReturn(new Value("n", "*"));
    
    when(request.getValue("e")).thenReturn(new Value("e", null));
    when(request.getValue("i", "1")).thenReturn(new Value("i", "1"));
    when(request.getValue("corus:process_id")).thenReturn(new Value("corus:process_id", "1234"));
    when(request.getValue("pr")).thenReturn(new Value("pr", "*:*"));
  }

  @Test
  public void testExecProcessesForCluster() throws Exception {
    resource.execProcessesForCluster(context);
    verify(processor).exec(any(ProcessCriteria.class), eq(1), any(ClusterInfo.class));
  }

  @Test
  public void testExecProcessesForHost() throws Exception {
    resource.execProcessesForHost(context);
    verify(processor).exec(any(ProcessCriteria.class), eq(1), any(ClusterInfo.class));
  }

  @Test
  public void testKillProcessesForCluster() throws Exception {
    resource.killProcessesForCluster(context);
    verify(processor).kill(any(ProcessCriteria.class), any(KillPreferences.class), any(ClusterInfo.class));
  }

  @Test
  public void testKillProcessesForHost() throws Exception {
    resource.killProcessesForHost(context);
    verify(processor).kill(any(ProcessCriteria.class), any(KillPreferences.class), any(ClusterInfo.class));
  }

  @Test
  public void testKillProcessForId() throws Exception {
    resource.killProcessForId(context);
    verify(processor).kill(any(ProcessCriteria.class), eq(KillPreferences.newInstance()), any(ClusterInfo.class));
  }

  @Test
  public void testSuspendProcessesForCluster() throws Exception {
    resource.suspendProcessesForCluster(context);
    verify(processor).suspend(any(ProcessCriteria.class), eq(KillPreferences.newInstance().setSuspend(true)), any(ClusterInfo.class));
  }

  @Test
  public void testSuspendProcessesForHost() throws Exception {
    resource.suspendProcessesForHost(context);
    verify(processor).suspend(any(ProcessCriteria.class), eq(KillPreferences.newInstance().setSuspend(true)), any(ClusterInfo.class));
  }

  @Test
  public void testSuspendProcessForId() throws Exception {
    resource.suspendProcessForId(context);
    verify(processor).suspend(any(ProcessCriteria.class), eq(KillPreferences.newInstance().setSuspend(true)), any(ClusterInfo.class));
  }

  @Test
  public void testResumeProcessesForCluster() throws Exception {
    resource.resumeProcessesForCluster(context);
    verify(processor).resume(any(ProcessCriteria.class), any(ClusterInfo.class));
  }

  @Test
  public void testResumeProcessesForHost() throws Exception {
    resource.resumeProcessesForHost(context);
    verify(processor).resume(any(ProcessCriteria.class), any(ClusterInfo.class));
  }

  @Test
  public void testResumeProcessForId() throws Exception {
    resource.resumeProcessForId(context);
    verify(processor).resume(any(ProcessCriteria.class), any(ClusterInfo.class));  
  }

  @Test
  public void testRestartProcessesForCluster() throws Exception {
    resource.restartProcessesForCluster(context);
    verify(processor).restart(any(ProcessCriteria.class), eq(KillPreferences.newInstance()), any(ClusterInfo.class));
  }

  @Test
  public void testRestartProcessesForHost() throws Exception {
    resource.restartProcessesForHost(context);
    verify(processor).restart(any(ProcessCriteria.class), eq(KillPreferences.newInstance()), any(ClusterInfo.class));
  }

  @Test
  public void testRestartProcessForId() throws Exception {
    resource.restartProcessForId(context);
    verify(processor).restart(any(ProcessCriteria.class), eq(KillPreferences.newInstance()), any(ClusterInfo.class));  
  }

  @Test
  public void testCleanProcessesForCluster() throws Exception {
    resource.cleanProcessesForCluster(context);
  }

  @Test
  public void testCleanProcessesForHost() throws Exception {
    resource.cleanProcessesForHost(context);
  }

}
