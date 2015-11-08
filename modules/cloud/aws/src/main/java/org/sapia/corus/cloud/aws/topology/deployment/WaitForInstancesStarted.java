package org.sapia.corus.cloud.aws.topology.deployment;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.RetryLatch;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Machine;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResourceSummary;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.google.common.base.Preconditions;

/**
 * Waits for all instances in a CloudFormation to be in the RUNNING state.
 * 
 * @author yduchesne
 *
 */
public class WaitForInstancesStarted implements
    WorkflowStep<AwsTopologyDeploymentContext> {

  private static final String RESOURCE_TYPE_EC2_INSTANCE = "AWS::EC2::Instance";
  
  private static final String DESC = "waiting for EC2 instances to be in running state";
  
  @Override
  public String getDescription() {
    return DESC;
  }

  @Override
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
    Topology topology = context.getSettings().getNotNull("topology").get(Topology.class);
    String   envName  = context.getSettings().getNotNull("environment").get(String.class);
    Env      env      = topology.getEnvByName(envName);

    RetryCriteria criteria   = context.getSettings().get("instancesRunningCheckRetry").get(RetryCriteria.class);
    RetryLatch    latch      = new RetryLatch(criteria);
    int expectedInstances    = 0;
    int runningInstances     = 0;
    int nonRunningInstances  = 0;

    for (Cluster cluster : env.getClusters()) {
      for (Machine m : cluster.getMachines()) {
        expectedInstances = m.getMinInstances();
      }
    }
    
    Preconditions.checkState(
        expectedInstances > 0, 
        "Invalid number of expected instances determined %s. Topology must be fixed", expectedInstances
    );

    context.getLog().info(
        "Checking that minimum number of instances (%s) is running across CloudFormation (%s)...", 
        expectedInstances, context.getStackId()
    );
    
    do {
      
      runningInstances    = 0;
      nonRunningInstances = 0;
      
      ListStackResourcesResult stackRes = context
          .getCloudFormationClient()
          .listStackResources(new ListStackResourcesRequest().withStackName(context.getStackId()));

      Preconditions.checkState(!stackRes.getStackResourceSummaries().isEmpty(),
          "Not CloudFormation found for ID: %s", context.getStackId());

      List<String> instanceIds = new ArrayList<String>();
      for (int i = 0; i < stackRes.getStackResourceSummaries().size(); i++) {
        StackResourceSummary rs = stackRes.getStackResourceSummaries().get(i);
        if (rs.getResourceType().equals(RESOURCE_TYPE_EC2_INSTANCE)) {
          instanceIds.add(rs.getPhysicalResourceId());
        }
      }
      
      DescribeInstanceStatusResult descRes = context.getEc2Client().describeInstanceStatus(
          new DescribeInstanceStatusRequest().withInstanceIds(instanceIds)
      );
      for (InstanceStatus status : descRes.getInstanceStatuses()) {
        if (status.getInstanceState().getCode().equals(InstanceStatusCode.RUNNING.value())) {
          runningInstances++;
        } else {
          nonRunningInstances++;
        }
      }
    } while (runningInstances < expectedInstances && latch.incrementAndPause().shouldContinue());
    
    context.getLog().info("Got %s instances running (expected at least %s).", runningInstances, expectedInstances);
    if (nonRunningInstances > 0) {
      context.getLog().info("Also: got %s instances in non-running (undetermined) state", nonRunningInstances);
    }
    
    Preconditions.checkState(
        runningInstances >= expectedInstances, 
        "%s/%s instances running for CloudFormation %s", 
        runningInstances, expectedInstances, context.getStackId()
    );
  }
}
