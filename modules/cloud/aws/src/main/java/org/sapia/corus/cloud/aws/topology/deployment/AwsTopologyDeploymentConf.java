package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.cloud.platform.settings.ReflectionSettings;
import org.sapia.corus.cloud.platform.settings.Settings;
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

  public static final int POLLING_INTERVAL_SECONDS = 2;
  
  private Topology             topology;
  private String               topologyVersionOverride;
  private String               environment;
  private File                 cloudFormationOutputDir = new File(System.getProperty("java.io.tmpdir"));
  private String               cloudFormationFileName;
  private Map<String, String>  globalTags              = new HashMap<String, String>();
  private List<File>           sourceTemplateDirs      = new ArrayList<File>();
  private List<TemplateLoader> sourceTemplateLoaders   = new ArrayList<TemplateLoader>();
  private boolean              isCreateStack           = true;
 
  private RetryCriteria  cloudFormationCreationCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CLOUDFORMATION_CREATION_MAX_WAIT)
  );
  
  private RetryCriteria    instancesRunningCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_INSTANCES_RUN_CHECK_MAX_WAIT)
  );
  
  public Settings asSettings() {
    validate();
    return new ReflectionSettings(this);
  }
  
  public void validate() {
    Preconditions.checkState(topology != null, "Topology not set");
    Preconditions.checkState(environment != null, "Environment not set");
    Preconditions.checkState(cloudFormationOutputDir != null, "Cloud formation output directory not set");
    
    if (cloudFormationFileName == null) {
      cloudFormationFileName =  String.format("%s-%s-%s-%s-%s.json", 
          topology.getOrg(), topology.getApplication(), topology.getVersion(), environment,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
      );
    }
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
}
