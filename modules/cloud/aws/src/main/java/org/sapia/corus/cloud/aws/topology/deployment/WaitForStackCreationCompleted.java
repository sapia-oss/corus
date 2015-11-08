package org.sapia.corus.cloud.aws.topology.deployment;

import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.RetryLatch;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;

/**
 * Waits for the completion of the CloudFormation stack.
 * 
 * @author yduchesne
 *
 */
public class WaitForStackCreationCompleted implements WorkflowStep<AwsTopologyDeploymentContext> {
 
  private static final String DESC = "waiting for completion of CloudFormation stack creation";
  
  public enum StackCreationStatus {
    CREATE_IN_PROGRESS, 
    CREATE_FAILED, 
    CREATE_COMPLETE, 
  }
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
  
    RetryLatch latch          = new RetryLatch(context.getSettings().getNotNull("cloudFormationCreationCheckRetry").get(RetryCriteria.class));
    String     expectedStatus = StackCreationStatus.CREATE_COMPLETE.name();
    String     actualStatus   = null;
    do {
      DescribeStacksRequest req = new DescribeStacksRequest();
      req.withStackName(context.getStackId());
      DescribeStacksResult result = context.getCloudFormationClient().describeStacks(req);
      if (result.getStacks().isEmpty()) {
        throw new IllegalStateException("No CloudFormation stack found for ID: " + context.getStackId());
      }
      Stack stack = result.getStacks().get(0);
      actualStatus = stack.getStackStatus();
      
      if (actualStatus.equals(StackCreationStatus.CREATE_FAILED.name())) {
        throw new IllegalStateException("Stack creation failed. Reason: " + stack.getStackStatusReason());
      }
    } while (!actualStatus.equals(expectedStatus) && latch.incrementAndPause().shouldContinue());
  }

}
