package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.cloud.aws.CloudFormationGenerator;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.google.common.base.MoreObjects;
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
    globalTags.putAll(context.getConf().getGlobalTags());
    
    String topologyVersion = MoreObjects.firstNonNull(
        context.getConf().getTopologyVersionOverride(), context.getConf().getTopology().getVersion()
    );
    
    globalTags.put(AwsTopologyDeploymentConsts.TOPOLOGY_VERSION_TAG_NAME, topologyVersion);
    
    CloudFormationGenerator.Builder builder = CloudFormationGenerator.Builder.newInstance()
        .withGlobalTags(context.getConf().getGlobalTags());
    
    for (File f : context.getConf().getSourceTemplateDirs()) {
      builder.withTemplateDir(f);
    }
    
    for (TemplateLoader t : context.getConf().getSourceTemplateLoaders()) {
      builder.withTemplateLoader(t);
    }
    
    CloudFormationGenerator generator = builder.build();
    
    File outputDir = context.getConf().getCloudFormationOutputDir();
    if (outputDir.exists()) {
      Preconditions.checkState(outputDir.isDirectory(), "Specified CloudFormation output directory is in fact not a directory: " + outputDir.getAbsolutePath());
    } else {
      outputDir.mkdirs();
    }
    
    Preconditions.checkState(outputDir.exists(), "CloudFormation output directory does not exist: " + outputDir.getAbsolutePath());
    
    File outputFile = new File(outputDir, context.getConf().getCloudFormationFileName());
    
    context.getLog().info("Generating cloud formation file: %s", outputFile.getAbsolutePath());
    generator.generateFile(context.getConf().getTopology(), context.getConf().getEnvironment(), outputFile);
    context.assignGeneratedCloudFormationFile(outputFile);
  }
  

}
