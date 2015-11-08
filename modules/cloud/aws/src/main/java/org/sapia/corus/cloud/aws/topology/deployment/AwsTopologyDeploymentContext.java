package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;

import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.workflow.WorkflowContext;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.google.common.base.Preconditions;

/**
 * Holds context pertaining to the topology deployment workflow.
 * 
 * @author yduchesne
 *
 */
public class AwsTopologyDeploymentContext extends WorkflowContext {

  private AmazonCloudFormation cloudFormationClient;
  private AmazonEC2            ec2Client;
  private String               stackId;
  private File                 generatedCloudFormationFile;
  
  public AwsTopologyDeploymentContext(Settings settings, 
      AmazonCloudFormation cloudFormationClient,
      AmazonEC2 ec2Client) {
    super(settings);
    this.cloudFormationClient = cloudFormationClient;
    this.ec2Client            = ec2Client;
  }
  
  public AwsTopologyDeploymentContext(AwsTopologyDeploymentConf conf, 
      AmazonCloudFormation cloudFormationClient,
      AmazonEC2 ec2Client) {
    this(conf.asSettings(), cloudFormationClient, ec2Client);
  }
  
  /**
   * @return the {@link AmazonCloudFormation} client.
   */
  public AmazonCloudFormation getCloudFormationClient() {
    return cloudFormationClient;
  }
  
  /**
   * @return the {@link AmazonEC2} client.
   */
  public AmazonEC2 getEc2Client() {
    return ec2Client;
  }
  
  public void assignGeneratedCloudFormationFile(File file) {
    this.generatedCloudFormationFile = file;
  }
  
  public File getGeneratedCloudFormationFile() {
    Preconditions.checkState(generatedCloudFormationFile != null, "Generated CloudFormation file not set");
    return generatedCloudFormationFile;
  }
  
  public void assignStackId(String stackId) {
    this.stackId = stackId;
  }
  
  public String getStackId() {
    Preconditions.checkState(stackId != null, "Stack ID not set");
    return stackId;
  }
  
}
