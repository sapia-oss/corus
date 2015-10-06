package org.sapia.corus.cloud.platform.workflow;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.workflow.GuardedExecutionCapable;
import org.sapia.corus.cloud.platform.workflow.WorkflowContext;
import org.sapia.corus.cloud.platform.workflow.WorkflowImpl;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowImplTest {
  
  private WorkflowImpl<WorkflowContext> successWfl, errorWfl, guardedWfl;
  
  @Mock
  private WorkflowStep<WorkflowContext> successStep1, successStep2, errStep;
  
  @Spy
  private TestGuardedStep  guarded;
  
  @Spy
  private PrerequisiteStep preReq;
  
  @Mock
  private Settings settings;
  
  private WorkflowContext context;
  
  @Before
  public void setUp() throws Exception {
    successWfl = new WorkflowImpl<WorkflowContext>(Mockito.mock(WorkflowLog.class), Lists.newArrayList(successStep1, successStep2));
    errorWfl   = new WorkflowImpl<WorkflowContext>(Mockito.mock(WorkflowLog.class), Lists.newArrayList(successStep1, errStep, successStep2));
    guardedWfl = new WorkflowImpl<WorkflowContext>(Mockito.mock(WorkflowLog.class), Lists.newArrayList(preReq, errStep, guarded));
    context    = new WorkflowContext(settings);
 
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new Exception("Error occurred");
      }
    }).when(errStep).execute(any(WorkflowContext.class));
  }
  
  @Test
  public void testExecute_success() {
    successWfl.execute(context);
    
    WorkflowResult res = successWfl.getResult();
    assertEquals(WorkflowResult.Outcome.SUCCESS, res.getOutcome());
    
    assertEquals(0, res.getFailureStepResults().size());
    assertEquals(2, res.getSuccessStepResults().size());
  }

  @Test
  public void testExecute_failure() {
    errorWfl.execute(context);
    
    WorkflowResult res = errorWfl.getResult();
    assertEquals(WorkflowResult.Outcome.FAILURE, res.getOutcome());
    
    assertEquals(1, res.getFailureStepResults().size());
    assertEquals(1, res.getSuccessStepResults().size());
  }
  
  @Test
  public void testExecute_guarded() throws Exception {
    guardedWfl.execute(context);
    
    WorkflowResult res = guardedWfl.getResult();
    assertEquals(WorkflowResult.Outcome.FAILURE, res.getOutcome());
    
    assertEquals(1, res.getFailureStepResults().size());
    assertEquals(2, res.getSuccessStepResults().size());
    
    verify(guarded).execute(any(WorkflowContext.class));
  }
  
  // --------------------------------------------------------------------------
  
  public class TestGuardedStep implements WorkflowStep<WorkflowContext>, GuardedExecutionCapable {
    
    @Override
    public String getDescription() {
      return "test";
    }
    
    @Override
    public void execute(WorkflowContext context) throws Exception {
    }
    
    @Override
    public Set<Class<?>> getGuardedExecutionPrerequisites() {
      Set<Class<?>> toReturn = new HashSet<Class<?>>();
      toReturn.add(PrerequisiteStep.class);
      return toReturn;
    }
  }
  
  public class PrerequisiteStep implements WorkflowStep<WorkflowContext> {
    
    @Override
    public String getDescription() {
      return "test";
    }
    
    @Override
    public void execute(WorkflowContext context) throws Exception {
      
    }
  }
  
}
