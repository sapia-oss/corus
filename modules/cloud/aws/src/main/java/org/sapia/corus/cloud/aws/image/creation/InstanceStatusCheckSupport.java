package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.RetryLatch;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.base.Preconditions;

/**
 * An abstract step implementing logic for checking the status of a running instance 
 * that's just been launched, and waiting until that status is "reached". 
 * 
 * @author yduchesne
 *
 */
public abstract class InstanceStatusCheckSupport implements WorkflowStep<ImageCreationContext> {
  
  

  // ==========================================================================
  
  private String             desc;
  private InstanceStatusCode expectedCode;
  
  protected InstanceStatusCheckSupport(String desc, InstanceStatusCode expectedCode) {
    this.desc   = desc;
    this.expectedCode  = expectedCode;
  }
  
  @Override
  public String getDescription() {
    return desc;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    DescribeInstancesRequest describeInstancesReq = new DescribeInstancesRequest();
    describeInstancesReq.withInstanceIds(context.getStartedInstanceId());
    int           statusCode = -1;
    RetryLatch    latch      = new RetryLatch(doGetRetryCriteria(context));
    InstanceState state      = null;
    do {
      try {
        DescribeInstancesResult describeInstancesRes = context.getEc2Client().describeInstances(describeInstancesReq);
        Preconditions.checkState(!describeInstancesRes.getReservations().isEmpty(), "Could not find instance matching given ID: " + context.getStartedInstanceId());
        Reservation reservation = describeInstancesRes.getReservations().get(0);
       
        Preconditions.checkState(!reservation.getInstances().isEmpty(), "Could not find instance matching given ID: " + context.getStartedInstanceId());
        Instance      instance = reservation.getInstances().get(0);
        state                  = instance.getState();
        statusCode             = state.getCode();
        if (state.getCode() != expectedCode.value()) {
         context.getLog().verbose("Current instance state is: %s. Expected: %s", state.getName().toUpperCase(), expectedCode.name());
        }
        
      } catch (AmazonServiceException e) {
        if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
          // this condition is due to an AWS glitch: this instance is there (since at this stage we have the instance ID)
          // the error is probably due to a timing issue within AWS' infra
          latch.pause();
        } else {
          throw e;
        }
        
      }
    } while (statusCode != expectedCode.value() && latch.incrementAndPause().shouldContinue());
    Preconditions.checkState(statusCode != -1, "Instance %s does not seem to exist", context.getStartedInstanceId());
    Preconditions.checkState(statusCode == expectedCode.value(), "Invalid status: %s. Expected: %s", state.getName().toUpperCase(), expectedCode.name());
  }

  protected abstract RetryCriteria doGetRetryCriteria(ImageCreationContext context);
}
