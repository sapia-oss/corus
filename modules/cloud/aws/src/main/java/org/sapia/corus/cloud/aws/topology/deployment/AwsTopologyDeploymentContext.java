package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.domain.DeploymentJournal;
import org.sapia.corus.cloud.platform.http.HttpClientFactory;
import org.sapia.corus.cloud.platform.http.JdkHttpClientFactory;
import org.sapia.corus.cloud.platform.rest.CorusRestClientFactory;
import org.sapia.corus.cloud.platform.rest.DefaultCorusRestClientFactory;
import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.util.Input;
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

  private AmazonCloudFormation   cloudFormationClient;
  private AmazonEC2              ec2Client;
  private String                 stackId;
  private Input                  generatedCloudFormationFile;
  private List<CorusInstance>    corusInstances = new ArrayList<>();
  private DeploymentJournal      deploymentJournal = new DeploymentJournal();
  private CorusRestClientFactory restClientFactory = new DefaultCorusRestClientFactory();
  private HttpClientFactory      httpClientFactory = new JdkHttpClientFactory();
  
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
  
  /**
   * @return this instance's {@link DeploymentJournal}.
   */
  public DeploymentJournal getDeploymentJournal() {
    return deploymentJournal;
  }  
  
  /**
   * @param factory the {@link CorusRestClientFactory} to use.
   * @return this instance.
   */
  public AwsTopologyDeploymentContext withRestClientFactory(CorusRestClientFactory factory) {
    this.restClientFactory = factory;
    return this;
  }
  
  /**
   * @return the {@link CorusRestClientFactory} to use.
   */
  public CorusRestClientFactory getRestClientFactory() {
    return restClientFactory;
  }
  
  /**
   * @param factory the {@link HttpClientFactory} to use.
   * @return this instance.
   */
  public AwsTopologyDeploymentContext withHttpClientFactory(HttpClientFactory factory) {
    this.httpClientFactory = factory;
    return this;
  }
  
  /**
   * @return a new {@link HttpClientFactory}.
   */
  public HttpClientFactory getHttpClientFactory() {
    return httpClientFactory;
  }
  
  public void assignGeneratedCloudFormationFile(final File file) {
    this.generatedCloudFormationFile = new Input() {
      @Override
      public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
      }
      
      @Override
      public String getInfo() {
        try {
          return file.getCanonicalPath();
        } catch (IOException e) {
          throw new IllegalStateException("Could not obtain canonical path for file: " + file.getName(), e);
        }
      }
    };
  }
  
  public void assignGeneratedCloudFormationFile(Input input) {
    this.generatedCloudFormationFile = input;
  }
  
  public Input getGeneratedCloudFormationFile() {
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
  
  public void addCorusInstance(CorusInstance instance) {
    corusInstances.add(instance);
  }
  
  public List<CorusInstance> getCorusInstances() {
    return Collections.unmodifiableList(corusInstances);
  }
}
