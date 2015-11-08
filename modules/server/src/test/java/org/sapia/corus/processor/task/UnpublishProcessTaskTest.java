package org.sapia.corus.processor.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingCompletedEvent;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingCompletedEvent.UnpublishStatus;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingPendingEvent;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.client.services.pub.UnpublishingCallback;

@RunWith(MockitoJUnitRunner.class)
public class UnpublishProcessTaskTest extends TestBaseTask {

  @Mock
  private ProcessPublisher     publisher;
 
  @Mock
  private EventDispatcher      dispatcher;
  
  private Process process;
  
  private Distribution dist;
  
  private ProcessConfig processConf;
  
  private ActivePort activePort;
  
  private UnpublishProcessTask task;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    process = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    dist = new Distribution("test", "1.0");
    processConf = new ProcessConfig("testProcess");
    activePort = new ActivePort("test-port", 8080);
    
    ctx.getServices().rebind(ProcessPublisher.class, publisher);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    
    task = new UnpublishProcessTask();
  }

  @Test
  public void testExecute_unpublish_succesful() throws Throwable {
    setUpPublisherForSuccess();
    tm.executeAndWait(task, process).get();
  
    verify(dispatcher).dispatch(isA(ProcessUnpublishingPendingEvent.class));
    verify(dispatcher).dispatch(eq(new ProcessUnpublishingCompletedEvent(process, UnpublishStatus.SUCCESS)));
  }
  
  @Test
  public void testExecute_unpublish_failure() throws Throwable {
    setUpPublisherForFailure();
    tm.executeAndWait(task, process).get();
  
    verify(dispatcher).dispatch(isA(ProcessUnpublishingPendingEvent.class));
    verify(dispatcher).dispatch(eq(new ProcessUnpublishingCompletedEvent(process, UnpublishStatus.FAILURE)));
  }
  
  @Test
  public void testExecute_unpublish_not_applicable() throws Throwable {
    setUpPublisherForNotApplicable();
    tm.executeAndWait(task, process).get();
  
    verify(dispatcher).dispatch(isA(ProcessUnpublishingPendingEvent.class));
    verify(dispatcher).dispatch(eq(new ProcessUnpublishingCompletedEvent(process, UnpublishStatus.NOT_APPLICABLE)));
  }
  
  private void setUpPublisherForSuccess() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Process p = invocation.getArgumentAt(0, Process.class);
        UnpublishingCallback cb = invocation.getArgumentAt(1, UnpublishingCallback.class);
        cb.unpublishingSuccessful(new ProcessPubContext(p, dist, processConf, activePort, mock(ProcessPubConfig.class)));
        return null;
      }
    }).when(publisher).unpublishProcess(eq(process), any(UnpublishingCallback.class));
  }
  
  private void setUpPublisherForFailure() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Process p = invocation.getArgumentAt(0, Process.class);
        UnpublishingCallback cb = invocation.getArgumentAt(1, UnpublishingCallback.class);
        cb.unpublishingFailed(new ProcessPubContext(p, dist, processConf, activePort, mock(ProcessPubConfig.class)), 
            new Exception("Unpublishing failed"));
        return null;
      }
    }).when(publisher).unpublishProcess(eq(process), any(UnpublishingCallback.class));
  }

  private void setUpPublisherForNotApplicable() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Process p = invocation.getArgumentAt(0, Process.class);
        UnpublishingCallback cb = invocation.getArgumentAt(1, UnpublishingCallback.class);
        cb.unpublishingNotApplicable(p);
        return null;
      }
    }).when(publisher).unpublishProcess(eq(process), any(UnpublishingCallback.class));
  }
}
