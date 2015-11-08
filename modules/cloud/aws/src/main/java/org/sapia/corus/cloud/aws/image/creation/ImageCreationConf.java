package org.sapia.corus.cloud.aws.image.creation;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.cloud.aws.image.userdata.UserDataPopulatorChain;
import org.sapia.corus.cloud.platform.settings.ReflectionSettings;
import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.google.common.base.Preconditions;

public class ImageCreationConf {
  
  public static final String DEFAULT_REGION        = "us-west-2";
  public static final String DEFAULT_CHEF_VERSION  = "12.4.1";
  public static final int    DEFAULT_CORUS_PORT    = 33443;
  public static final String DEFAULT_IMAGE_PREFIX  = "corus-ami";
  
  public static final int DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT   = 10;
  public static final int DEFAULT_CORUS_INSTALL_MAX_WAIT        = 25;
  public static final int DEFAULT_INSTANCE_STOP_MAX_WAIT        = 5;
  public static final int DEFAULT_IMG_CREATION_MAX_WAIT         = 10;
  public static final int DEFAULT_INSTANCE_TERMINATION_MAX_WAIT = 5;

  public static final int POLLING_INTERVAL_SECONDS = 2;
  
  private UserDataPopulatorChain userData = UserDataPopulatorChain.getDefaultInstance();
  
  private AWSCredentials   awsCredentials;
  private List<String>     cookbooks             = new ArrayList<String>();
  private String           recipeAttributes;
  private String           region                = DEFAULT_REGION;
  private String           imageId;
  private List<String>     securityGroups        = new ArrayList<String>();
  private String           subnetId;
  private String           keypair;
  private String           iamRole;
  private String           chefVersion           = DEFAULT_CHEF_VERSION;
  private boolean          isYumUpdate           = true;
  private boolean          isAwsCliInstall       = true;
  private int              corusPort             = DEFAULT_CORUS_PORT;
  private String           corusImageNamePrefix  = DEFAULT_IMAGE_PREFIX;
  
  private RetryCriteria    instanceRunCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT)
  );
  private RetryCriteria    corusIntallCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CORUS_INSTALL_MAX_WAIT)
  );
  private RetryCriteria    instanceStopCheckRetry = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_STOP_MAX_WAIT)
  );
  private RetryCriteria    imgCreationCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_IMG_CREATION_MAX_WAIT)
  );
  private RetryCriteria    instanceTerminatedCheckRetry = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCE_TERMINATION_MAX_WAIT)
  );
  
  public UserDataPopulatorChain getUserDataPopulators() {
    return userData;
  }

  public ImageCreationConf withUserDataPopulators(UserDataPopulatorChain userDataPopulators) {
    this.userData = userDataPopulators;
    return this;
  }

  public AWSCredentials getAwsCredentials() {
    if (awsCredentials == null) {
      awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
    }
    return awsCredentials;
  }

  public ImageCreationConf withAwsCredentials(AWSCredentials awsCredentials) {
    this.awsCredentials = awsCredentials;
    return this;
  }

  public List<String> getCookbooks() {
    return cookbooks;
  }

  public ImageCreationConf withCookbooks(List<String> cookbooks) {
    this.cookbooks = cookbooks;
    return this;
  }

  public String getRecipeAttributes() {
    return recipeAttributes;
  }

  public ImageCreationConf withRecipeAttributes(String recipeAttributes) {
    this.recipeAttributes = recipeAttributes;
    return this;
  }

  public String getRegion() {
    return region;
  }

  public ImageCreationConf withRegion(String region) {
    this.region = region;
    return this;
  }

  public String getImageId() {
    return imageId;
  }

  public ImageCreationConf withImageId(String imageId) {
    this.imageId = imageId;
    return this;
  }

  public List<String> getSecurityGroups() {
    return securityGroups;
  }

  public ImageCreationConf withSecurityGroups(List<String> securityGroups) {
    this.securityGroups = securityGroups;
    return this;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public ImageCreationConf withSubnetId(String subnetId) {
    this.subnetId = subnetId;
    return this;
  }

  public String getKeypair() {
    return keypair;
  }

  public ImageCreationConf withKeypair(String keypair) {
    this.keypair = keypair;
    return this;
  }

  public String getIamRole() {
    return iamRole;
  }

  public ImageCreationConf withIamRole(String iamRole) {
    this.iamRole = iamRole;
    return this;
  }

  public String getChefVersion() {
    return chefVersion;
  }

  public ImageCreationConf withChefVersion(String chefVersion) {
    this.chefVersion = chefVersion;
    return this;
  }

  public boolean isYumUpdate() {
    return isYumUpdate;
  }

  public ImageCreationConf withYumUpdate(boolean isYumUpdate) {
    this.isYumUpdate = isYumUpdate;
    return this;
  }

  public boolean isAwsCliInstall() {
    return isAwsCliInstall;
  }

  public ImageCreationConf withAwsCliInstall(boolean isAwsCliInstall) {
    this.isAwsCliInstall = isAwsCliInstall;
    return this;
  }

  public int getCorusPort() {
    return corusPort;
  }

  public ImageCreationConf withCorusPort(int corusPort) {
    this.corusPort = corusPort;
    return this;
  }

  public String getCorusImageNamePrefix() {
    return corusImageNamePrefix;
  }

  public ImageCreationConf withCorusImageNamePrefix(String corusImageNamePrefix) {
    this.corusImageNamePrefix = corusImageNamePrefix;
    return this;
  }

  public RetryCriteria getInstanceRunCheckRetry() {
    return instanceRunCheckRetry;
  }

  public ImageCreationConf withInstanceRunCheckRetry(RetryCriteria instanceRunCheckRetry) {
    this.instanceRunCheckRetry = instanceRunCheckRetry;
    return this;
  }

  public RetryCriteria getCorusIntallCheckRetry() {
    return corusIntallCheckRetry;
  }

  public ImageCreationConf withCorusIntallCheckRetry(RetryCriteria corusIntallCheckRetry) {
    this.corusIntallCheckRetry = corusIntallCheckRetry;
    return this;
  }

  public RetryCriteria getInstanceStopCheckRetry() {
    return instanceStopCheckRetry;
  }

  public ImageCreationConf withInstanceStopCheckRetry(RetryCriteria instanceStopCheckRetry) {
    this.instanceStopCheckRetry = instanceStopCheckRetry;
    return this;
  }

  public RetryCriteria getImgCreationCheckRetry() {
    return imgCreationCheckRetry;
  }

  public ImageCreationConf withImgCreationCheckRetry(RetryCriteria imgCreationCheckRetry) {
    this.imgCreationCheckRetry = imgCreationCheckRetry;
    return this;
  }

  public RetryCriteria getInstanceTerminatedCheckRetry() {
    return instanceTerminatedCheckRetry;
  }

  public ImageCreationConf withInstanceTerminatedCheckRetry(
      RetryCriteria instanceTerminatedCheckRetry) {
    this.instanceTerminatedCheckRetry = instanceTerminatedCheckRetry;
    return this;
  }
  
  public Settings asSettings() {
    validate();
    return new ReflectionSettings(this);
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