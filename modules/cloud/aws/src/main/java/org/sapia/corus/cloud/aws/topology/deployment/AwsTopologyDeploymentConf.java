package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  
  public static final int DEFAULT_CLOUDFORMATION_CREATION_MAX_WAIT = 20;

  public static final int POLLING_INTERVAL_SECONDS = 2;
  
  Topology             topology;
  String               topologyVersionOverride;
  String               environment;
  File                 cloudFormationOutputDir = new File(System.getProperty("java.io.tmp.dir"));
  String               cloudFormationFileName;
  Map<String, String>  globalTags              = new HashMap<String, String>();
  List<File>           sourceTemplateDirs      = new ArrayList<File>();
  List<TemplateLoader> sourceTemplateLoaders   = new ArrayList<TemplateLoader>();
  boolean              createStack;
 
  RetryCriteria    cloudFormationCreationCheckRetry  = RetryCriteria.forMaxDuration(
      TimeMeasure.forSeconds(POLLING_INTERVAL_SECONDS), TimeMeasure.forMinutes(DEFAULT_CLOUDFORMATION_CREATION_MAX_WAIT)
  );
  
  public void validate() {
    Preconditions.checkState(topology != null, "Topology not set");
    Preconditions.checkState(environment != null, "Environment not set");
    Preconditions.checkState(cloudFormationOutputDir != null, "Cloud formation output directory not set");
  }
  
  public Topology getTopology() {
    return topology;
  }
  
  public String getEnvironment() {
    return environment;
  }
  
  public File getCloudFormationOutputDir() {
    return cloudFormationOutputDir;
  }
  
  public String getCloudFormationFileName() {
    if (cloudFormationFileName == null) {
      return String.format("%s-%s-%s-%s-%s.json", 
          topology.getOrg(), topology.getApplication(), topology.getVersion(), environment,
          new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
      );
    }
    return cloudFormationFileName;
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
  
  public boolean isCreateStack() {
    return createStack;
  }
  
  public String getTopologyVersionOverride() {
    return topologyVersionOverride;
  }
  
  public RetryCriteria getCloudFormationCreationCheckRetry() {
    return cloudFormationCreationCheckRetry;
  }
}
