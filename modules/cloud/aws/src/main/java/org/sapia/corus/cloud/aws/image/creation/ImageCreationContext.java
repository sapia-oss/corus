package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.image.EC2ImageConf;
import org.sapia.corus.cloud.platform.workflow.WorkflowContext;

import com.amazonaws.services.ec2.AmazonEC2;
import com.google.common.base.Preconditions;

public class ImageCreationContext extends WorkflowContext {

  private EC2ImageConf conf;
  private AmazonEC2    ec2Client;
  
  private String allocatedPublicIp, ipAllocationId, startedInstanceId, createdImageId;
  
  public ImageCreationContext(EC2ImageConf conf, AmazonEC2 ec2Client) {
    this.conf      = conf;
    this.ec2Client = ec2Client;
  }
  
  public EC2ImageConf getConf() {
    return conf;
  }
  
  public AmazonEC2 getEc2Client() {
    return ec2Client;
  }
  
  public ImageCreationContext assignStartedInstanceId(String instanceId) {
    this.startedInstanceId = instanceId;
    return this;
  }
  
  public ImageCreationContext assignAllocatedPublicIp(String ip, String allocationId) {
    this.allocatedPublicIp = ip;
    this.ipAllocationId    = allocationId;
    return this;
  }
  
  public ImageCreationContext assignCreatedImageId(String imageId) {
    this.createdImageId = imageId;
    return this;
  }
  
  public String getAllocatedPublicIp() {
    Preconditions.checkState(allocatedPublicIp != null, "Public IP not allocated (a public IP should be allocated" 
        + " in order for the instance to be accessible for checking proper deployment (using Corus' ping endoint)");
    return allocatedPublicIp;
  }
  
  public String getIpAllocationId() {
    Preconditions.checkState(allocatedPublicIp != null, "Public IP not allocated (not allocation ID available)");
    return ipAllocationId;
  }
  
  public String getStartedInstanceId() {
    Preconditions.checkState(startedInstanceId != null, "Instance ID not available (check that instance was previously started)");
    return startedInstanceId;
  }
  
  public String getCreatedImageId() {
    Preconditions.checkState(createdImageId != null, "Image ID not available (check that image was previously created)");
    return createdImageId;
  }
}
