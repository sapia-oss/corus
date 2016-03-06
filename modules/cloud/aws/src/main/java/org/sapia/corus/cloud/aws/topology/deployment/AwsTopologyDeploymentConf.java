package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.settings.ReflectionSettings;
import org.sapia.corus.cloud.platform.settings.Settings;
import org.sapia.corus.cloud.platform.util.Input;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.topology.Topology;

import com.google.common.base.Preconditions;

import freemarker.cache.TemplateLoader;

/**
 * 
 * 
 * @author yduchesne
 *
 */
public class AwsTopologyDeploymentConf {
  
  public static final int DEFAULT_INSTANCES_RUN_CHECK_MAX_WAIT = 20;

  public static final int DEFAULT_CLOUDFORMATION_CREATION_MAX_WAIT = 20;
  
  public static final int DEFAULT_CORUS_INSTANCE_RUNNING_MAX_WAIT = 20;
  
  public static final int DEFAULT_POLLING_INTERVAL_SECONDS = 5;

  public static final int DEFAULT_HTTP_CLIENT_TIMEOUT_SECONDS = 5;
  
  public static final int DEFAULT_CORUS_MAX_CONNECT_RETRY = 6;
  
  public static final int DEFAULT_CORUS_PORT = 33000;
  
  private Topology             topology;
  private String               topologyVersionOverride;
  private String               environment;
  private File                 cloudFormationOutputDir = new File(System.getProperty("java.io.tmpdir"));
  private String               cloudFormationFileName;
  private Map<String, String>  globalTags              = new HashMap<String, String>();
  private List<File>           sourceTemplateDirs      = new ArrayList<File>();
  private List<TemplateLoader> sourceTemplateLoaders   = new ArrayList<TemplateLoader>();
  private boolean              isCreateStack           = true;
  private TimeMeasure          httpClientTimeout       = TimeMeasure.forSeconds(DEFAULT_HTTP_CLIENT_TIMEOUT_SECONDS);
  private TimeMeasure          pollingInterval         = TimeMeasure.forSeconds(DEFAULT_POLLING_INTERVAL_SECONDS);
  
  private int                  corusRestMaxErrors;
  
  private int                  corusRestMinHosts;
  
  private int                  corusRestBatchSize;
  
  private CorusCredentials     corusCredentials;
  
  private int                  corusPort               = DEFAULT_CORUS_PORT;
  private int                  corusMaxConnectRetry    = DEFAULT_CORUS_MAX_CONNECT_RETRY;
 
  private RetryCriteria cloudFormationCreationCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(DEFAULT_POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CLOUDFORMATION_CREATION_MAX_WAIT)
  );
  
  private RetryCriteria instancesRunningCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(DEFAULT_POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCES_RUN_CHECK_MAX_WAIT)
  );
  
  private RetryCriteria corusInstanceCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(DEFAULT_POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CORUS_INSTANCE_RUNNING_MAX_WAIT)
  );
  
  private List<Input>  distributions = new ArrayList<>();
  
  public Settings asSettings() {
    validate();
    return new ReflectionSettings(this);
  }
  
  public void validate() {
    Preconditions.checkState(topology != null, "Topology not set");
    Preconditions.checkState(environment != null, "Environment not set");
    Preconditions.checkState(cloudFormationOutputDir != null, "Cloud formation output directory not set");
    Preconditions.checkState(corusCredentials != null, "Corus credentials not set");
    
    if (cloudFormationFileName == null) {
      cloudFormationFileName =  String.format("%s-%s-%s-%s-%s.json", 
          topology.getOrg(), topology.getApplication(), topology.getVersion(), environment,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
      );
    }
  }
  
  public AwsTopologyDeploymentConf withCorusPort(int port) {
    this.corusPort = port;
    return this;
  }
  
  public int getCorusPort() {
    return corusPort;
  }
  
  public AwsTopologyDeploymentConf withCorusMaxConnectRetry(int maxRetry) {
    this.corusMaxConnectRetry = maxRetry;
    return this;
  }
  
  public int getCorusMaxConnectRetry() {
    return corusMaxConnectRetry;
  }
  
  public AwsTopologyDeploymentConf withCorusRestMaxErrors(int maxErrors) {
    this.corusRestMaxErrors = maxErrors;
    return this;
  }
  
  public int getCorusRestMaxErrors() {
    return corusRestMaxErrors;
  }

  public AwsTopologyDeploymentConf withCorusRestMinHosts(int minHosts) {
    this.corusRestMinHosts = minHosts;
    return this;
  }
  
  public int getCorusRestMinHosts() {
    return corusRestMinHosts;
  }
  
  public AwsTopologyDeploymentConf withCorusRestBatchSize(int batchSize) {
    this.corusRestBatchSize = batchSize;
    return this;
  }
  
  public int getCorusRestBatchSize() {
    return corusRestBatchSize;
  }
  
  public AwsTopologyDeploymentConf withCorusCredentials(CorusCredentials credentials) {
    this.corusCredentials = credentials;
    return this;
  }
  
  public CorusCredentials getCorusCredentials() {
    return corusCredentials;
  }
  
  public AwsTopologyDeploymentConf withDistributions(List<Input> dists) {
    this.distributions = dists;
    return this;
  }
  
  public List<Input> getDistributions() {
    return distributions;
  }
  
  public AwsTopologyDeploymentConf withInstancesRunningCheckRetry(RetryCriteria criteria) {
    instancesRunningCheckRetry = criteria;
    return this;
  }
  
  public RetryCriteria getInstancesRunningCheckRetry() {
    return instancesRunningCheckRetry;
  }
  
  public AwsTopologyDeploymentConf withCorusInstanceCheckRetry(RetryCriteria criteria) {
    corusInstanceCheckRetry = criteria;
    return this;
  }
  
  public RetryCriteria getCorusInstanceCheckRetry() {
    return corusInstanceCheckRetry;
  }
  
  public AwsTopologyDeploymentConf withHttpClientTimeout(TimeMeasure timeout) {
    httpClientTimeout = timeout;
    return this;
  }
  
  public TimeMeasure getHttpClientTimeout() {
    return httpClientTimeout;
  }
  
  public AwsTopologyDeploymentConf withTopology(Topology topology) {
    this.topology = topology;
    return this;
  }
  
  public Topology getTopology() {
    return topology;
  }
  
  public AwsTopologyDeploymentConf withEnvironment(String environment) {
    this.environment = environment;
    return this;
  }
  
  public String getEnvironment() {
    return environment;
  }
  
  public AwsTopologyDeploymentConf withCloudFormationOutputDir(File cloudFormationOutputDir) {
    this.cloudFormationOutputDir = cloudFormationOutputDir;
    return this;
  }
   
  public File getCloudFormationOutputDir() {
    return cloudFormationOutputDir;
  }
  
  public AwsTopologyDeploymentConf withCloudFormationFileName(String cloudFormationFileName) {
    this.cloudFormationFileName = cloudFormationFileName;
    return this;
  }
  
  public String getCloudFormationFileName() {

    return cloudFormationFileName;
  }
  
  public void setGlobalTags(Map<String, String> globalTags) {
    this.globalTags = globalTags;
  }
  
  public Map<String, String> getGlobalTags() {
    return globalTags;
  }
  
  public void addSourceTemplateDirs(File...templateDirs) {
    sourceTemplateDirs.addAll(Arrays.asList(templateDirs));
  }
  
  public List<File> getSourceTemplateDirs() {
    return sourceTemplateDirs;
  }
  
  public void addSourceTemplateLoaders(TemplateLoader...templateLoaders) {
    sourceTemplateLoaders.addAll(Arrays.asList(templateLoaders));
  }
  
  public List<TemplateLoader> getSourceTemplateLoaders() {
    return sourceTemplateLoaders;
  }

  public AwsTopologyDeploymentConf withCreateStack(boolean create) {
    isCreateStack = create;
    return this;
  }
  
  public boolean isCreateStack() {
    return isCreateStack;
  }
  
  public AwsTopologyDeploymentConf withTopologyVersionOverride(String topologyVersionOverride) {
    this.topologyVersionOverride = topologyVersionOverride;
    return this;
  }
  
  public String getTopologyVersionOverride() {
    return topologyVersionOverride;
  }
  
  public AwsTopologyDeploymentConf withCloudFormationCreationCheckRetry(RetryCriteria criteria) {
    this.cloudFormationCreationCheckRetry = criteria;
    return this;
  }
  
  public RetryCriteria getCloudFormationCreationCheckRetry() {
    return cloudFormationCreationCheckRetry;
  }
  
  public TimeMeasure getPollingInterval() {
    return pollingInterval;
  }
}
