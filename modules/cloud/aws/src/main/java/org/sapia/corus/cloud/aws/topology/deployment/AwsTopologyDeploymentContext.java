package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;

import org.sapia.corus.cloud.platform.workflow.WorkflowContext;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.google.common.base.Preconditions;

/**
 * Holds context pertaining to the topology deployment workflow.
 * 
 * @author yduchesne
 *
 */
public class AwsTopologyDeploymentContext extends WorkflowContext {

  private AwsTopologyDeploymentConf conf;
  private AmazonCloudFormation      cloudFormationClient;
  
  private String stackId;
  private File generatedCloudFormationFile;
  
  public AwsTopologyDeploymentContext(AwsTopologyDeploymentConf conf, AmazonCloudFormation cloudFormationClient) {
    this.conf                 = conf;
    this.cloudFormationClient = cloudFormationClient;
  }

  /**
   * @return the {@link AwsTopologyDeploymentConf}.
   */
  public AwsTopologyDeploymentConf getConf() {
    return conf;
  }
  
  /**
   * @return the {@link AmazonCloudFormation} client.
   */
  public AmazonCloudFormation getCloudFormationClient() {
    return cloudFormationClient;
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
