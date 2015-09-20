package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.image.userdata.UserDataContext;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.google.common.base.Preconditions;

/**
 * Starts the EC2 instance from which an image will be created.
 * 
 * @author yduchesne
 *
 */
public class InvokeRunInstance implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "Starting instance from which image will be created";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  public void execute(ImageCreationContext context) {
    RunInstancesRequest runReq    = new RunInstancesRequest(context.getConf().getImageId(), 1, 1);
    runReq.withInstanceType(InstanceType.T2Small);
    if (context.getConf().getIamRole() != null) {
      runReq.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(context.getConf().getIamRole()));
    }
    runReq.withKeyName(context.getConf().getKeypair());
    runReq.withSecurityGroupIds(context.getConf().getSecurityGroups());
    if (context.getConf().getSubnetId() != null) {
      runReq.withSubnetId(context.getConf().getSubnetId());
    }
    
    UserDataContext ctx = new UserDataContext(context.getConf());
    context.getConf().getUserDataPopulators().addTo(ctx);

    runReq.withUserData(ctx.getUserData().toByte64(context.getLog()));
    
    RunInstancesResult runResult = context.getEc2Client().runInstances(runReq);
    Preconditions.checkState(!runResult.getReservation().getInstances().isEmpty(), "Instance was not started, check the AWS console for more info");
    
    String instanceId = runResult.getReservation().getInstances().get(0).getInstanceId();
    context.assignStartedInstanceId(instanceId);
    
  }

}
