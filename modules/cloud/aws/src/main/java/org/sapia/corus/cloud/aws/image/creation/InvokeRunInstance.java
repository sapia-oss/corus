package org.sapia.corus.cloud.aws.image.creation;

import java.util.List;

import org.sapia.corus.cloud.aws.image.userdata.UserDataContext;
import org.sapia.corus.cloud.aws.image.userdata.UserDataPopulatorChain;
import org.sapia.corus.cloud.platform.settings.Setting;
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
  
  private static final String DESC = "requesting startup of instance used for image creation";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @SuppressWarnings("unchecked")
  public void execute(ImageCreationContext context) {
    RunInstancesRequest runReq    = new RunInstancesRequest(context.getSettings().getNotNull("imageId").get(String.class), 1, 1);
  
    runReq.withInstanceType(InstanceType.T2Small);
    
    Setting iamRole = context.getSettings().getNotNull("iamRole");
    runReq.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(iamRole.get(String.class)));
    
    runReq.withKeyName(context.getSettings().getNotNull("keypair").get(String.class));
    runReq.withSecurityGroupIds((List<String>)context.getSettings().getNotNull("securityGroups").get(List.class));
  
    runReq.withSubnetId(context.getSettings().getNotNull("subnetId").get(String.class));
    
    UserDataContext userDataContext = new UserDataContext(context.getSettings());
    context.getSettings().getNotNull("userData").get(UserDataPopulatorChain.class).addTo(userDataContext);
    runReq.withUserData(userDataContext.getUserData().toByte64(context.getLog()));
    
    RunInstancesResult runResult = context.getEc2Client().runInstances(runReq);
    Preconditions.checkState(
        !runResult.getReservation().getInstances().isEmpty(), 
        "Instance was not started, check the AWS console for more info"
    );
    
    String instanceId = runResult.getReservation().getInstances().get(0).getInstanceId();
    context.assignStartedInstanceId(instanceId);
  }

}
