package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.cloud.platform.settings.Setting;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.topology.Topology;

import com.google.common.base.Preconditions;

import freemarker.cache.TemplateLoader;

/**
 * Generates a CloudFormation file.
 * 
 * @author yduchesne
 *
 */
public class GenerateCloudFormationFile implements WorkflowStep<AwsTopologyDeploymentContext> {

  private static final String DESC = "generating CloudFormation file from template";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
    Map<String, String> globalTags = new HashMap<String, String>();
    globalTags.putAll(context.getSettings().getNotNull("globalTags").getMapOf(String.class, String.class));
    
    Setting  topologyOverride = context.getSettings().get("topologyVersionOverride");
    Topology topology         = context.getSettings().getNotNull("topology").get(Topology.class);
    String   topologyVersion  = topologyOverride.isSet() ? topologyOverride.get(String.class) : topology.getVersion();
    
    globalTags.put(AwsTopologyDeploymentConsts.TOPOLOGY_VERSION_TAG_NAME, topologyVersion);
    
    CloudFormationGenerator.Builder builder = CloudFormationGenerator.Builder.newInstance()
        .withGlobalTags(globalTags);
    
    for (File f : context.getSettings().getNotNull("sourceTemplateDirs").getListOf(File.class)) {
      builder.withTemplateDir(f);
    }
    
    for (TemplateLoader t : context.getSettings().getNotNull("sourceTemplateLoaders").getListOf(TemplateLoader.class)) {
      builder.withTemplateLoader(t);
    }
    
    CloudFormationGenerator generator = builder.build();
    
    File outputDir = context.getSettings().getNotNull("cloudFormationOutputDir").get(File.class);
    if (outputDir.exists()) {
      Preconditions.checkState(outputDir.isDirectory(), "Specified CloudFormation output directory is in fact not a directory: " + outputDir.getAbsolutePath());
    } else {
      outputDir.mkdirs();
    }
    
    Preconditions.checkState(outputDir.exists(), "CloudFormation output directory does not exist: " + outputDir.getAbsolutePath());
    
    File outputFile = new File(outputDir, context.getSettings().getNotNull("cloudFormationFileName").get(String.class));
    
    context.getLog().info("Generating cloud formation file: %s", outputFile.getAbsolutePath());
    generator.generateFile(topology, context.getSettings().getNotNull("environment").get(String.class), outputFile);
    context.assignGeneratedCloudFormationFile(outputFile);
  }
  

}
