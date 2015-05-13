package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.corus.cloud.aws.image.creation.InstanceStatusCheckSupport.InstanceStatusCode;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Reservation;

public class InstanceStatusCheckSupportTest {

  private ImageCreationContext       context;
  private InstanceStatusCheckSupport step;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    context.assignStartedInstanceId("testInstanceId");
    step = new InstanceStatusCheckSupport("test", InstanceStatusCode.RUNNING) {
      @Override
      protected RetryCriteria doGetRetryCriteria(ImageCreationContext context) {
        return RetryCriteria.forMaxAttempts(TimeMeasure.forMillis(200), 3);
      }
    };
  }
  
  @Test
  public void testExecute_success() throws Exception {
    doAnswer(new Answer<DescribeInstancesResult>() {
      int retryCount;
      @Override
      public DescribeInstancesResult answer(InvocationOnMock invocation)
          throws Throwable {
        retryCount++;
        if (retryCount > 1) {
          return new DescribeInstancesResult()
            .withReservations(new Reservation().withInstances(
                new Instance().withInstanceId("testInstanceId").withState(
                    new InstanceState().withCode(InstanceStatusCode.RUNNING.value()).withName(InstanceStateName.Running))));      
        } else {
          return new DescribeInstancesResult()
          .withReservations(new Reservation().withInstances(
              new Instance().withInstanceId("testInstanceId").withState(
                  new InstanceState().withCode(InstanceStatusCode.PENDING.value()).withName(InstanceStateName.Pending))));      
        }
      }
    }).when(context.getEc2Client()).describeInstances(any(DescribeInstancesRequest.class));
    
    step.execute(context);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testExecute_failure() throws Exception {
    doAnswer(new Answer<DescribeInstancesResult>() {
      @Override
      public DescribeInstancesResult answer(InvocationOnMock invocation)
          throws Throwable {
        return new DescribeInstancesResult()
          .withReservations(new Reservation().withInstances(
              new Instance().withInstanceId("testInstanceId").withState(
                  new InstanceState().withCode(InstanceStatusCode.PENDING.value()).withName(InstanceStateName.Pending))));      
      }
    }).when(context.getEc2Client()).describeInstances(any(DescribeInstancesRequest.class));
    
    step.execute(context);
  }
}
