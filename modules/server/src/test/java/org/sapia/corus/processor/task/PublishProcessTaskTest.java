package org.sapia.corus.processor.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessPublishingCompletedEvent;
import org.sapia.corus.client.services.processor.event.ProcessPublishingCompletedEvent.PublishStatus;
import org.sapia.corus.client.services.processor.event.ProcessPublishingPendingEvent;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;

@RunWith(MockitoJUnitRunner.class)
public class PublishProcessTaskTest extends TestBaseTask {
 
  private static final int MAX_EXEC = 3;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private InternalServiceContext serviceContext;
  
  @Mock
  private EventDispatcher dispatcher;
  
  @Mock
  private DiagnosticModule diagnosticModule;
  
  @Mock
  private ProcessPublisher publisher;
  
  private Process process;
  
  private ProcessConfig processConf;
  
  private ActivePort activePort;
  
  private PublishProcessTask task;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    process = new Process(new DistributionInfo("test", "1.0", "prod", "testProcess"));
    processConf = new ProcessConfig("testProcess");
    activePort = new ActivePort("test-port", 8080);
    
    task    = new PublishProcessTask(OptionalValue.of(MAX_EXEC));
    
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    ctx.getServices().rebind(DiagnosticModule.class, diagnosticModule);
    ctx.getServices().rebind(ProcessPublisher.class, publisher);
    
    when(serverContext.getServices()).thenReturn(serviceContext);
    when(serviceContext.getEventDispatcher()).thenReturn(dispatcher);
    when(serviceContext.getDiagnosticModule()).thenReturn(diagnosticModule);
    when(serviceContext.getProcessPublisher()).thenReturn(publisher);
    when(diagnosticModule.acquireDiagnosticFor(eq(process), any(LockOwner.class)))
      .thenReturn(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "success", process));
  }

  @Test
  public void testExecute_process_locked() throws Throwable {
    LockOwner owner = LockOwner.createInstance();
    process.getLock().acquire(owner);
    
    tm.executeAndWait(task, process).get();
    
    verify(serviceContext, never()).getDiagnosticModule();
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
  }
  
  @Test
  public void testExecute_publishing_successful() throws Throwable {
    setUpPublisherForSuccess();
    
    tm.executeAndWait(task, process).get();
    
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
  }
  
  @Test
  public void testExecute_publishing_failure() throws Throwable {
    setUpPublisherForFailure();
    
    tm.executeAndWait(task, process).get();
    
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.FAILURE));
  }
  
  @Test
  public void testExecute_publishing_not_applicable() throws Throwable {
    setUpPublisherForNotApplicable();
    
    tm.executeAndWait(task, process).get();
    
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.NOT_APPLICABLE));
  }

  @Test
  public void testExecute_diagnostic_incomplete_multiple_attempts_success() throws Throwable {
    reset(diagnosticModule);
    doAnswer(new Answer<ProcessDiagnosticResult>() {
      int count = 0;
      @Override
      public ProcessDiagnosticResult answer(InvocationOnMock invocation)
          throws Throwable {
        if (count >= 1) {
          return new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "success", process);
        } else {
          count++;
          return new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "incomplete", process);
        }
      }
    }).when(diagnosticModule).acquireDiagnosticFor(eq(process), any(LockOwner.class));
    
    setUpPublisherForSuccess();
    
    tm.executeAndWait(task, process).get();
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher, never()).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
    
    reset(dispatcher);

    tm.executeAndWait(task, process).get();
    verify(dispatcher, never()).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
  }
  
  @Test
  public void testExecute_process_lock_multiple_attempts_success() throws Throwable {
    setUpPublisherForSuccess();

    LockOwner owner = LockOwner.createInstance();
    process.getLock().acquire(owner);
    
    tm.executeAndWait(task, process).get();
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher, never()).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
    
    process.getLock().release(owner);
    reset(dispatcher);

    tm.executeAndWait(task, process).get();
    verify(dispatcher, never()).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
  }
  
  
  @Test
  public void testExecute_diagnostic_incomplete_multiple_attempts_failure() throws Throwable {
    reset(diagnosticModule);
    when(diagnosticModule.acquireDiagnosticFor(eq(process), any(LockOwner.class)))
      .thenReturn(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, "incomplete", process));
    
    for (int i = 0; i < MAX_EXEC + 1; i++) {
      tm.executeAndWait(task, process).get();
    }
    verify(dispatcher).dispatch(isA(ProcessPublishingPendingEvent.class));
    verify(dispatcher, never()).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.SUCCESS));
    verify(dispatcher).dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.MAX_ATTEMPTS_REACHED));
  }
  
  private void setUpPublisherForSuccess() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        PublishingCallback cb = invocation.getArgumentAt(1, PublishingCallback.class);
        cb.publishingSuccessful(new ProcessPubContext(process, processConf, activePort, mock(ProcessPubConfig.class)));
        return null;
      }
    }).when(publisher).publishProcess(any(Process.class), any(PublishingCallback.class));
  }

  private void setUpPublisherForFailure() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        PublishingCallback cb = invocation.getArgumentAt(1, PublishingCallback.class);
        cb.publishingFailed(new ProcessPubContext(process, processConf, activePort, mock(ProcessPubConfig.class)), new Exception("Publishing failed"));
        return null;
      }
    }).when(publisher).publishProcess(any(Process.class), any(PublishingCallback.class));
  }
  
  private void setUpPublisherForNotApplicable() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        PublishingCallback cb = invocation.getArgumentAt(1, PublishingCallback.class);
        cb.publishingNotApplicable(process);
        return null;
      }
    }).when(publisher).publishProcess(any(Process.class), any(PublishingCallback.class));
  }
}
