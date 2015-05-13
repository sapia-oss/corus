package org.sapia.corus.cloud.aws.image;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.cloud.aws.image.userdata.UserDataPopulatorChain;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;

import com.amazonaws.auth.AWSCredentials;
import com.google.common.base.Preconditions;

public class EC2ImageConf {
  
  public static final String DEFAULT_REGION        = "us-west-2";
  public static final String DEFAULT_CHEF_VERSION  = "12.4.1";
  public static final int    DEFAULT_CORUS_PORT    = 33000;
  public static final String DEFAULT_IMAGE_PREFIX  = "corus-ami";
  
  public static final int DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT   = 10;
  public static final int DEFAULT_CORUS_INSTALL_MAX_WAIT        = 25;
  public static final int DEFAULT_INSTANCE_STOP_MAX_WAIT        = 5;
  public static final int DEFAULT_IMG_CREATION_MAX_WAIT         = 10;
  public static final int DEFAULT_INSTANCE_TERMINATION_MAX_WAIT = 5;

  public static final int POLLING_INTERVAL_SECONDS = 2;
  
  UserDataPopulatorChain userDataPopulators = UserDataPopulatorChain.getDefaultInstance();
  
  AWSCredentials   awsCredentials;
  List<String>     cookbooks             = new ArrayList<String>();
  String           recipeAttributes;
  String           region                = DEFAULT_REGION;
  String           imageId;
  List<String>     securityGroups        = new ArrayList<String>();
  String           subnetId;
  String           keypair;
  String           iamRole;
  String           chefVersion           = DEFAULT_CHEF_VERSION;
  boolean          isYumUpdate           = true;
  boolean          isAwsCliInstall       = true;
  int              corusPort             = DEFAULT_CORUS_PORT;
  String           corusImageNamePrefix  = DEFAULT_IMAGE_PREFIX;
  WorkflowLog      log                   = DefaultWorkflowLog.getDefault();
  
  RetryCriteria    instanceRunCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT)
  );
  RetryCriteria    corusIntallCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CORUS_INSTALL_MAX_WAIT)
  );
  RetryCriteria    instanceStopCheckRetry = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_STOP_MAX_WAIT)
  );
  RetryCriteria    imgCreationCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_IMG_CREATION_MAX_WAIT)
  );
  RetryCriteria    instanceTerminatedCheckRetry = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_TERMINATION_MAX_WAIT)
  );
  
  public AWSCredentials getAwsCredentials() {
    return awsCredentials;
  }

  public List<String> getCookbooks() {
    return cookbooks;
  }

  public String getRecipeAttributes() {
    return recipeAttributes;
  }

  public String getRegion() {
    return region;
  }

  public String getImageId() {
    return imageId;
  }

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public String getKeypair() {
    return keypair;
  }

  public String getIamRole() {
    return iamRole;
  }
  
  public String getChefVersion() {
    return chefVersion;
  }
  
  public boolean isYumUpdate() {
    return isYumUpdate;
  }
  
  public boolean isAwsCliInstall(){
    return isAwsCliInstall;
  }
  
  public UserDataPopulatorChain getUserDataPopulators() {
    return userDataPopulators;
  }
  
  public int getCorusPort() {
    return corusPort;
  }
  
  public RetryCriteria getInstanceRunCheckRetry() {
    return instanceRunCheckRetry;
  }
  
  public RetryCriteria getCorusIntallCheckRetry() {
    return corusIntallCheckRetry;
  }
  
  public RetryCriteria getInstanceStopCheckRetry() {
    return instanceStopCheckRetry;
  }
  
  public RetryCriteria getImageCreationCheckRetry() {
    return imgCreationCheckRetry;
  }
  
  public RetryCriteria getInstanceTerminatedCheckRetry() {
    return instanceTerminatedCheckRetry;
  }

  public String getCorusImageNamePrefix() {
    return corusImageNamePrefix;
  }
  
  public WorkflowLog getLog() {
    return log;
  }

  public void validate() {
    Preconditions.checkState(awsCredentials != null, "AWS credentials not set");
    Preconditions.checkState(keypair != null, "Name of keypair not set");
    Preconditions.checkState(recipeAttributes != null, "Recipe attributes not set");
    Preconditions.checkState(imageId != null, "Image ID not set");
    Preconditions.checkState(region != null, "Region not set");
    Preconditions.checkState(subnetId != null, "Subnet ID not set");
    Preconditions.checkState(iamRole != null, "IAM role not set");
  }
  
}