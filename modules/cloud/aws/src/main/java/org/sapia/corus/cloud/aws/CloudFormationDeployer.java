package org.sapia.corus.cloud.aws;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Wraps an {@link AmazonCloudFormationClient} in order 
 * 
 * @author yduchesne
 *
 */
public class CloudFormationDeployer {

  /**
   * A generic CloudFormation create/delete request visitor: such a visitor is invoked
   * after a request instance has been created an initialized with its basic parameters, but before
   * it is sent to the cloud.
   * 
   * @author yduchesne
   *
   */
  public interface RequestVisitor<R> {
    
    public void visit(R request);
    
  }
  
  public static class NullCreateStackRequestVisitor implements RequestVisitor<CreateStackRequest> {
  
    public static final NullCreateStackRequestVisitor INSTANCE = new NullCreateStackRequestVisitor();
    
    @Override
    public void visit(CreateStackRequest request) {
    }
    
  }
  
  public static class NullDeleteStackRequestVisitor implements RequestVisitor<DeleteStackRequest> {
    
    public static final NullDeleteStackRequestVisitor INSTANCE = new NullDeleteStackRequestVisitor();
    
    @Override
    public void visit(DeleteStackRequest request) {
    }
    
  }
  
  // ==========================================================================
  
  private AmazonCloudFormationClient client;
  
  private String  stackName;
  private File    cloudFormation;
  private Charset charset           = Charsets.UTF_8;

  
  /**
   * Creates a new instance of this class, internally instantiating a default {@link AmazonCloudFormationClient}.
   */
  public CloudFormationDeployer() {
    this(new AmazonCloudFormationClient());
  }
  
  /**
   * @param credentials the {@link AWSCredentials} to use.
   * @param conf the {@link ClientConfiguration} to use.
   */
  public CloudFormationDeployer(AWSCredentials credentials, ClientConfiguration conf) {
    this(new AmazonCloudFormationClient(credentials, conf));
  }
  
  /**
   * @param client the {@link AmazonCloudFormationClient} to use.
   */
  public CloudFormationDeployer(AmazonCloudFormationClient client) {
    this.client = client;
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * @param stackName the stack name to assign to the CloudFormation.
   * @return this instance.
   */
  public CloudFormationDeployer withStackName(String stackName) {
    this.stackName = stackName;
    return this;
  }
  
  /**
   * @param cloudFormation the {@link File} corresponding to the CloudFormation to create.
   * @return this instance.
   */
  public CloudFormationDeployer withCloudFormation(File cloudFormation) {
    this.cloudFormation = cloudFormation;
    return this;
  }
  
  /**
   * @param charset the {@link Charset} in which the CloudFormation is written.
   * @return this instance.
   */
  public CloudFormationDeployer withCharSet(Charset charset) {
    this.charset = charset;
    return this;
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Creates the CloudFormation stack corresponding to this instance's parameters (namely:
   * this instance's stack name and CloudFormation configuration). 
   * <p>
   * Passes the {@link DeleteStackRequest} to the given visitor prior to proceeding 
   * to the stack's deletion.
   * 
   * @param visitor a {@link RequestVisitor} to invoke

   */
  public String createStack(RequestVisitor<CreateStackRequest> visitor) throws IOException {
    Preconditions.checkNotNull(stackName, "CloudFormation stack name not set");
    Preconditions.checkNotNull(cloudFormation, "CloudFormation file not set");
    Preconditions.checkNotNull(charset, "Character encoding of CloudFormation file not set");
    
    CreateStackRequest request = new CreateStackRequest()
      .withStackName(stackName)
      .withTemplateBody(Files.toString(cloudFormation, charset));
    
    visitor.visit(request);
    
    CreateStackResult result = client.createStack(request);
    return result.getStackId();
    
  }
 
  /**
   * Deletes the CloudFormation stack corresponding to this instance's stack name. 
   * <p> 
   * Passes the {@link DeleteStackRequest} to the given visitor prior to proceeding 
   * to the stack's deletion.
   * 
   * @param visitor a {@link RequestVisitor} to invoke.
   */
  public void deleteStack(RequestVisitor<DeleteStackRequest> visitor) {
    Preconditions.checkNotNull(stackName, "CloudFormation stack name not set");
    
    DeleteStackRequest request = new DeleteStackRequest()
      .withStackName(stackName);
    
    client.deleteStack(request);
  }
}
