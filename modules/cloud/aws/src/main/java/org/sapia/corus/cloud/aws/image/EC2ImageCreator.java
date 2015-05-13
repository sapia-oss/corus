package org.sapia.corus.cloud.aws.image;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.sapia.corus.cloud.aws.image.creation.ImageCreationContext;
import org.sapia.corus.cloud.aws.image.creation.ImageCreationWorkflowFactory;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.Workflow;
import org.sapia.corus.cloud.platform.workflow.WorkflowDiagnosticsHelper;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.common.io.Files;

/**
 * 
 * @author yduchesne
 *
 */
public class EC2ImageCreator {
  
  /**
   * A builder of {@link EC2ImageCreator} instances.
   * 
   * @author yduchesne
   *
   */
  public static final class Builder {

    private EC2ImageConf conf = new EC2ImageConf();
    
    /**
     * @param imageId the ID of a base image to use.
     * @return this instance.
     */
    public Builder withImage(String imageId) {
      this.conf.imageId = imageId;
      return this;
    }
    
    /**
     * @param region an Amazon region ID (optional - defaults to <code>us-west-1</code>).
     * @return this instance.
     */
    public Builder withRegion(String region) {
      this.conf.region = region;
      return this;
    }

    /**
     * @param cookbooks one or more Chef cookbook names.
     * @return this instance.
     */
    public Builder withCookbooks(String... cookbooks) {
      this.conf.cookbooks.addAll(Arrays.asList(cookbooks));
      return this;
    }
    
    /**
     * @param f the {@link File} corresponding to the JSON file holding recipe attributes - if any.
     * @param charset the {@link Charset} in which the given file is encoded (optional).
     * @return this instance.
     */
    public Builder withRecipeAttributes(File f, Charset charset) {
      try {
        this.conf.recipeAttributes = Files.toString(f, charset);
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not load recipe attribute file: " + f.getAbsolutePath(), e);
      }
      return this;
    }
    
    /**
     * @param json a JSON document holding recipe attributes (optional).
     * @return this instance.
     */
    public Builder withRecipeAttributes(String json) {
      this.conf.recipeAttributes = json;
      return this;
    }
    
    /**
     * @param credentials the {@link AWSCredentials} instance to use to connect to the AWS API (optional - if not
     * provided, the default credentials lookup algorithm will be applied).
     * @return this instance.
     * @see DefaultAWSCredentialsProviderChain
     */
    public Builder withAwsCredentials(AWSCredentials credentials) {
      this.conf.awsCredentials = credentials;
      return this;
    }
    
    public Builder withSecurityGroups(String...groups) {
      this.conf.securityGroups.addAll(Arrays.asList(groups));
      return this;
    }
    
    public Builder withSubnet(String subnetId) {
      this.conf.subnetId = subnetId;
      return this;
    }
    
    public Builder withIamRole(String roleName) {
      this.conf.iamRole = roleName;
      return this;
    }
    
    /**
     * @param keypair the name of the keypair to use for login into the EC2 instance.
     * @return this instance.
     */
    public Builder withKeyPair(String keypair) {
      this.conf.keypair = keypair;
      return this;
    }
    
    /**
     * @param isYumUpdate if <code>true</code>, indicates that a yum update should be performed when the instance
     * used for image creation boots up.
     * @return this instance.
     */
    public Builder withYumUpdate(boolean isYumUpdate) {
      this.conf.isYumUpdate = isYumUpdate;
      return this;
    }
    
    /**
     * @param isAwsCliInstall if <code>true</code>, indicates that the <code>awscli</code> executable should be installed on the machine
     * @return
     */
    public Builder withAwsCli(boolean isAwsCliInstall) {
      this.conf.isAwsCliInstall = isAwsCliInstall;
      return this;
    }
    
    /**
     * @param log the {@link WorkflowLog} to use.
     * @return this instance.
     */
    public Builder withWorkflowLog(WorkflowLog log) {
      this.conf.log = log;
      return this;
    }
    
    /**
     * @param imageNamePrefix the image name prefix to use (see {@link EC2ImageConf#DEFAULT_IMAGE_PREFIX}).
     * @return this instance.
     */
    public Builder withImageNamePrefix(String imageNamePrefix) {
      this.conf.corusImageNamePrefix = imageNamePrefix;
      return this;
    }
    
    /**
     * @param version the Chef version to use (see {@link EC2ImageConf#DEFAULT_CHEF_VERSION}).
     * @return
     */
    public Builder withChefVersion(String version) {
      this.conf.chefVersion = version;
      return this;
    }
    
    /**
     * @param port the port of the Corus server that will be installed. Will be used for pinging the
     * Corus instance in order to determine installation completion (see {@link EC2ImageConf#DEFAULT_CORUS_PORT}).
     * @return this instance.
     */
    public Builder withCorusPort(int port) {
      this.conf.corusPort = port;
      return this;
    }

    /**
     * @param minutes the maximum number of minutes to wait for Corus installation completion (see {@link EC2ImageConf#DEFAULT_CORUS_INSTALL_MAX_WAIT}.
     * @return this instance.
     */
    public Builder withCorusInstallCheckMaxWait(int minutes) {
      this.conf.corusIntallCheckRetry = RetryCriteria.forMaxDuration(
          TimeMeasure.forSeconds(EC2ImageConf.POLLING_INTERVAL_SECONDS),
          TimeMeasure.forMinutes(minutes)
      );
      return this;
    }
    
    /**
     * @param minutes the maximum number of minutes to wait for image creation completion (see {@link EC2ImageConf#DEFAULT_IMG_CREATION_MAX_WAIT}.
     * @return this instance.
     */
    public Builder withImgCreationMaxWait(int minutes) {
      this.conf.imgCreationCheckRetry = RetryCriteria.forMaxDuration(
          TimeMeasure.forSeconds(EC2ImageConf.POLLING_INTERVAL_SECONDS),
          TimeMeasure.forMinutes(minutes)
      );
      return this;
    }
    
    /**
     * @param minutes the maximum number of minutes to wait for the instance from which an image was created to terminate
     * (see {@link EC2ImageConf#DEFAULT_INSTANCE_TERMINATION_MAX_WAIT}.
     * @return this instance.
     */
    public Builder withInstanceRunMaxWait(int minutes) {
      this.conf.instanceTerminatedCheckRetry = RetryCriteria.forMaxDuration(
          TimeMeasure.forSeconds(EC2ImageConf.POLLING_INTERVAL_SECONDS),
          TimeMeasure.forMinutes(minutes)
      );
      return this;
    }

    /**
     * @param minutes the maximum number of minutes to wait for the instance to stop (after Corus has been installed).
     * (see {@link EC2ImageConf#DEFAULT_INSTANCE_STOP_MAX_WAIT}.
     * @return this instance.
     */
    public Builder withInstanceStopMaxWait(int minutes) {
      this.conf.instanceStopCheckRetry = RetryCriteria.forMaxDuration(
          TimeMeasure.forSeconds(EC2ImageConf.POLLING_INTERVAL_SECONDS),
          TimeMeasure.forMinutes(minutes)
      );
      return this;
    }
    
    /**
     * @param minutes the maximum number of minutes to wait for the instance to stop (after Corus has been installed).
     * (see {@link EC2ImageConf#DEFAULT_INSTANCE_TERMINATION_MAX_WAIT}.
     * @return this instance.
     */
    public Builder withInstanceTerminationMaxWait(int minutes) {
      this.conf.instanceTerminatedCheckRetry = RetryCriteria.forMaxDuration(
          TimeMeasure.forSeconds(EC2ImageConf.POLLING_INTERVAL_SECONDS),
          TimeMeasure.forMinutes(minutes)
      );
      return this;
    }
    
    /**
     * @return a new instance of this class.
     */
    public static final Builder newInstance() {
      return new Builder();
    }

    public EC2ImageCreator build() {
      if (conf.awsCredentials == null) {
        DefaultAWSCredentialsProviderChain chain = new DefaultAWSCredentialsProviderChain();
        chain.refresh();
        conf.awsCredentials = chain.getCredentials();
      }
      conf.validate();
      return new EC2ImageCreator(conf);
    }
    
   }
  
  // ==========================================================================
  // EC2ImageBaker class
  
  private EC2ImageConf conf;

  private EC2ImageCreator(EC2ImageConf conf) {
    this.conf = conf;
  }

  /**
   * Creates an image using the provided configuration.
   * 
   */
  public WorkflowResult createImage() {
    
    AmazonEC2Client     ec2Client = new AmazonEC2Client(conf.awsCredentials);
    
    try {
      WorkflowLog                    log = DefaultWorkflowLog.getDefault();
      Workflow<ImageCreationContext> wf  = ImageCreationWorkflowFactory.getDefaultWorkFlow(log);
      ImageCreationContext           ctx = new ImageCreationContext(conf, ec2Client);
      wf.execute(ctx);
      return wf.getResult();
    } finally {
      ec2Client.shutdown();
    }
    
  }
  
  /*
  private void doBake() throws Exception {
    
   
    DescribeRegionsResult  regionRes = ec2Client.describeRegions();
    Collection<Region> regionCandidates = Collections2.filter(regionRes.getRegions(), new Predicate<Region>() {
      @Override
      public boolean apply(Region input) {
        return input.getRegionName().equals(conf.region);
      }
    });
    
    Preconditions.checkState(!regionCandidates.isEmpty(), "No region matched: " + conf.region);
    ec2Client.withRegion(com.amazonaws.regions.Region.getRegion(Regions.fromName(conf.region)));
    
    RunInstancesRequest runReq    = new RunInstancesRequest(conf.imageId, 1, 1);
    runReq.withInstanceType(InstanceType.T2Small);
    if (conf.iamRole != null) {
      runReq.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(conf.iamRole));
    }
    runReq.withKeyName(conf.keypair);
    runReq.withSecurityGroupIds(conf.securityGroups);
    if (conf.subnetId != null) {
      runReq.withSubnetId(conf.subnetId);
    }
    
    AllocateAddressRequest allocateReq = new AllocateAddressRequest();
    allocateReq.withDomain(DomainType.Vpc);
    AllocateAddressResult allocateRes = ec2Client.allocateAddress(allocateReq);
    String publicIp = allocateRes.getPublicIp();
    
    UserDataContext ctx = new UserDataContext(conf);
    conf.userDataPopulators.addTo(ctx);

    runReq.withUserData(ctx.getUserData().toByte64());
    
    RunInstancesResult runResult = ec2Client.runInstances(runReq);
    Preconditions.checkState(!runResult.getReservation().getInstances().isEmpty(), "Instance was not started, check the AWS console for more info");
    
    String instanceId = runResult.getReservation().getInstances().get(0).getInstanceId();
    
    DescribeInstancesRequest describeInstancesReq = new DescribeInstancesRequest();
    describeInstancesReq.withInstanceIds(instanceId);
    Instance instance = waitUntilStatus(ec2Client, STATUS_RUNNING, "RUNNING", describeInstancesReq);
    
    try {
      ec2Client.associateAddress(new AssociateAddressRequest(instanceId, publicIp));
      System.out.println(String.format("Instance %s was started. Waiting for Corus installation completion", instanceId));
      waitForCorusInstallationCompleted(publicIp, 33000);
    
      System.out.println(String.format("Stopping instance %s in order to create image from it", instanceId));
      StopInstancesRequest stopReq = new StopInstancesRequest();
      stopReq.withInstanceIds(instanceId);
      ec2Client.stopInstances(stopReq);
      waitUntilStatus(ec2Client, STATUS_STOPPED, "STOPPED", describeInstancesReq);

      System.out.println(String.format("Instance %s has stopped. Creating image", instanceId));
      CreateImageRequest createImgReq = new CreateImageRequest(instanceId, "corus-ami-" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date()));
      CreateImageResult createImgRes = ec2Client.createImage(createImgReq);
      
      System.out.println(String.format("Image created, now destroying instance %s", instanceId));
      TerminateInstancesRequest terminateReq = new TerminateInstancesRequest(Lists.newArrayList(instanceId));
      ec2Client.terminateInstances(terminateReq);
      System.out.println(String.format("Instance %s terminated. Image creation workflow completed", instanceId));
      
    } finally {
      ReleaseAddressRequest req = new ReleaseAddressRequest(publicIp);
      ec2Client.releaseAddress(req);
    }
    
  }
  
  private Instance waitUntilStatus(AmazonEC2Client ec2Client, int expectedStatusCode, String name, DescribeInstancesRequest describeInstancesReq) throws Exception {
    int statusCode = -1;
    Instance      instance = null;
    while (statusCode != expectedStatusCode) {
      try {
        DescribeInstancesResult describeInstancesRes = ec2Client.describeInstances(describeInstancesReq);
        Reservation   reservation = describeInstancesRes.getReservations().get(0);
        instance = reservation.getInstances().get(0);
        InstanceState state       = instance.getState();
        statusCode                = state.getCode();
        if (statusCode != expectedStatusCode) {
          System.out.println(String.format("Current instance state is: %s. Expected: %s", state.getName().toUpperCase(), name));
          Thread.sleep(2000);
        }
      } catch (AmazonServiceException e) {
        if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
          // this condition is due to an AWS glitch: this instance is there (since at this stage we have the instance ID)
         //  the error is probably due to a timing issue within AWS' infra
          Thread.sleep(2000);
        } else {
          throw e;
        }
        
      }
    }
    return instance;
  }
  
  private void waitForCorusInstallationCompleted(String host, int port) throws Exception {
    String urlString = "http://" + host + ":" + port + "/ping";
    System.out.println("Connecting to Corus: " + urlString);
    URL corusUrl = new URL(urlString);
    while (true) {
      try {
        String response = getCorusResponse(corusUrl);
        if (response.toLowerCase().contains("cloud_ready")) {
          System.out.println("Corus install completed");
          break;
        }
      } catch (IOException e) {
        System.out.println(String.format("I/O error caught while trying to connect to Corus: %s. Installation might not be finished, will retry", e.getMessage()));
      } 
      Thread.sleep(2000);
    }
  }
  
  private String getCorusResponse(URL corusUrl) throws IOException, IllegalStateException {
    HttpURLConnection corusConn = (HttpURLConnection) corusUrl.openConnection();
    corusConn.setDoOutput(false);
    corusConn.setDoInput(true);
    corusConn.setRequestMethod("GET");
    InputStream is = corusConn.getInputStream();
    try {
      String response = CharStreams.toString(new InputStreamReader(is));
      System.out.println("Got response from Corus: " + response);
      if (corusConn.getResponseCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Could not get installation status from Corus. HTTP response status: " + corusConn.getResponseCode());
      }
      return response;
    } finally {
      is.close();
      corusConn.disconnect();
    }    
  }
  */
  
  public static void main(String[] args) throws Exception {
    String jsonAttributes = ("{"
        + "'java': {'install_flavor': 'oracle', 'jdk_version': '8', 'oracle': {'accept_oracle_download_terms': true}},"
        + "'corus': {'version': '4.8', 'archive_download_url': 'http://www.mediafire.com/download/3bv6j3i84rn6kpx/sapia_corus_server_package-develop-SNAPSHOT-linux64.tar.gz'},"
        + "'run_list': ['tar', 'java', 'corus::avis_rpm', 'corus']"
        + "}").replace("'", "\"");
    
    System.out.println(jsonAttributes);
    
    BasicConfigurator.configure();
    
    WorkflowResult r = Builder.newInstance()
      .withImage("ami-73e7c443")
      .withSecurityGroups("sg-ddac92b8")
      .withKeyPair("yanick-keys-uswest")
      .withRegion("us-west-2")
      .withSubnet("subnet-daaf05ad")
      .withCookbooks("java", "tar", "corus")
      .withRecipeAttributes(jsonAttributes)
      .build().createImage();
    
    new WorkflowDiagnosticsHelper().displayDiagnostics(r);
  }
}
