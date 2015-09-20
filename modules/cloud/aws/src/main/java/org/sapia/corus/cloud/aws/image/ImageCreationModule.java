package org.sapia.corus.cloud.aws.image;

import java.io.File;

import org.apache.commons.codec.Charsets;
import org.sapia.console.CmdLine;
import org.sapia.console.Options;
import org.sapia.console.OptionDef;
import org.sapia.corus.cloud.aws.AwsConsts;
import org.sapia.corus.cloud.platform.cli.CliModule;
import org.sapia.corus.cloud.platform.cli.CliModuleContext;
import org.sapia.corus.cloud.platform.cli.CommandDefs;
import org.sapia.corus.cloud.platform.workflow.WorkflowDiagnosticsHelper;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult.Outcome;

/**
 * Executes the {@link CommandDefs#CREATE_IMAGE} command in the context of the AWS provider.
 * 
 * @author yduchesne
 *
 */
public class ImageCreationModule implements CliModule {
  
  private static final String DEFAULT_JSON_CHARSET       = "UTF-8";
  
  private static final String OPT_WITH_COOKBOOKS         = "with-cookbooks";
  private static final String OPT_WITH_RECIPE_ATTRIBUTES = "with-recipe-attributes";
  private static final String OPT_WITH_RECIPE_CHARSET    = "with-recipe-attributes-charset";
  private static final String OPT_WITH_IAM_ROLE          = "with-iam-role";
  private static final String OPT_WITH_IMAGE_ID          = "with-image-id";
  private static final String OPT_WITH_KET_PAIR          = "with-keypair";
  private static final String OPT_WITH_REGION            = "with-region";
  private static final String OPT_WITH_SECURITY_GROUPS   = "with-security-groups";
  private static final String OPT_WITH_SUBNET            = "with-subnet";
  private static final String OPT_WITH_YUM_UPDATE        = "with-yum-update";
  private static final String OPT_WITH_IMG_NAME_PREFIX   = "with-image-name-prefix";
  private static final String OPT_WITH_CORUS_PORT        = "with-corus-port";
  
  private static final String OPT_WITH_CORUS_INSTALL_WAIT = "with-corus-install-wait";
  private static final String OPT_WITH_INSTANCE_RUN_WAIT  = "with-instance-run-wait";
  private static final String OPT_WITH_INSTANCE_STOP_WAIT = "with-instance-stop-wait";
  private static final String OPT_WITH_INSTANCE_TERM_WAIT = "with-instance-termination-wait";
  private static final String OPT_WITH_IMG_CREATION_WAIT  = "with-image-creation-wait";
  
  
  public static Options OPTIONS = OptionDef.Builder.newInstance()
      .option().name(OPT_WITH_COOKBOOKS).desc("A comma-delimited list of chef cookbooks to install on the image").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_ATTRIBUTES).desc("A path to a JSON file holding Chef recipe attributes, and the run-list to execute").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_CHARSET).desc("The character set in which the JSON attribute file is encoded - defaults to " + DEFAULT_JSON_CHARSET).mustHaveValue()
      .option().name(OPT_WITH_IAM_ROLE).desc("The name of the IAM role under which to start the instance from which an image will be created").mustHaveValue()
      .option().name(OPT_WITH_IMAGE_ID).desc("The Amazon image ID corresponding to the image from which to create a new image").mustHaveValue().required()
      .option().name(OPT_WITH_KET_PAIR).desc("The name of the keypair to associate the instance from which an image will be created").mustHaveValue().required()
      .option().name(OPT_WITH_REGION).desc("The identifier of the AWS region under which to start the instance used for image creation - defaults to: " 
          + EC2ImageConf.DEFAULT_REGION).mustHaveValue()
      .option().name(OPT_WITH_SECURITY_GROUPS).desc("A comma-delimited list of security group IDs under which to run the instance used for image creation")
        .mustHaveValue().required()
      .option().name(OPT_WITH_SUBNET).desc("The ID of the subnet in which the instance used for image creation should be started." 
        + " Note that the provided security group IDs must be associated to the subnet being specified").mustHaveValue().required()
      .option().name(OPT_WITH_YUM_UPDATE).desc("Indicates if a yum update should be peformed on the instance used for image creation (true by default)").mustHaveValue()
      .option().name(OPT_WITH_IMG_NAME_PREFIX).desc("Specifies what image name prefix to use - defaults to " + EC2ImageConf.DEFAULT_IMAGE_PREFIX 
        + " A suffix is automatically appended which corresponds to the current date/time").mustHaveValue()
      .option().name(OPT_WITH_CORUS_PORT).desc("Indicates the port of the Corus server that will be installed. Used for pinging Corus in order to"
          + " determine installation completion - defaults to " + EC2ImageConf.DEFAULT_CORUS_PORT).mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_RUN_WAIT).desc("Max amount of time to wait for the instance (from which an image will be created) to stop " 
          + " - defaults to " + EC2ImageConf.DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT + " minutes").mustHaveValue()
      .option().name(OPT_WITH_CORUS_INSTALL_WAIT).desc("Max amount of time to wait for Corus installation completion - defaults to " 
          + EC2ImageConf.DEFAULT_CORUS_INSTALL_MAX_WAIT + " minutes").mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_STOP_WAIT).desc("Max mount of time to wait for the instance on which Corus was installed to stop" 
          + " (before an image is created from it). Defaults to " +  EC2ImageConf.DEFAULT_INSTANCE_STOP_MAX_WAIT + " minutes").mustHaveValue()
      .option().name(OPT_WITH_IMG_CREATION_WAIT).desc("Max amount of time wait for image creation (from the running Corus instance) - defaults"
          + " to " + EC2ImageConf.DEFAULT_IMG_CREATION_MAX_WAIT + "minutes").mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_TERM_WAIT).desc("Max amount of time to wait for the instance used to create the image to be terminated"
          + " - defaults to " + EC2ImageConf.DEFAULT_INSTANCE_TERMINATION_MAX_WAIT + " minutes").mustHaveValue()
      .option()
      .buildOptions();
      
  
  @Override
  public String getCommandName() {
    return CommandDefs.CREATE_IMAGE.getName();
  }
  
  @Override
  public String getProvider() {
    return AwsConsts.PROVIDER;
  }

  @Override
  public StatusCode interact(CliModuleContext context, CmdLine cmd) {
    EC2ImageCreator.Builder builder = EC2ImageCreator.Builder.newInstance();
    OPTIONS.validate(cmd);
    
    
    builder
      .withWorkflowLog(context.getWorflowLog())
      .withImage(cmd.getOptNotNull(OPT_WITH_IMAGE_ID).getValue())
      .withIamRole(cmd.getOptNotNull(OPT_WITH_IAM_ROLE).getValue())
      .withCookbooks(cmd.getOptOrDefault(OPT_WITH_COOKBOOKS, "tar,java,corus").getSplitValue())
      .withKeyPair(cmd.getOptNotNull(OPT_WITH_KET_PAIR).getValue())
      .withRegion(cmd.getSafeOpt(OPT_WITH_REGION).getValueOrDefault(EC2ImageConf.DEFAULT_REGION))
      .withSecurityGroups(cmd.getOptNotNull(OPT_WITH_SECURITY_GROUPS).getSplitValue())
      .withSubnet(cmd.getOptNotNull(OPT_WITH_SUBNET).getValue())
      .withYumUpdate(cmd.getSafeOpt(OPT_WITH_YUM_UPDATE).getValueOrDefault(true))
      .withImageNamePrefix(cmd.getOptOrDefault(OPT_WITH_IMG_NAME_PREFIX, EC2ImageConf.DEFAULT_IMAGE_PREFIX).getValue())
      .withCorusPort(cmd.getSafeOpt(OPT_WITH_CORUS_PORT).getValueOrDefault(EC2ImageConf.DEFAULT_CORUS_PORT))
      .withCorusInstallCheckMaxWait(cmd.getSafeOpt(OPT_WITH_CORUS_INSTALL_WAIT).getValueOrDefault(EC2ImageConf.DEFAULT_CORUS_INSTALL_MAX_WAIT))
      .withImgCreationMaxWait(cmd.getSafeOpt(OPT_WITH_IMG_CREATION_WAIT).getValueOrDefault(EC2ImageConf.DEFAULT_IMG_CREATION_MAX_WAIT))
      .withInstanceRunMaxWait(cmd.getSafeOpt(OPT_WITH_INSTANCE_RUN_WAIT).getValueOrDefault(EC2ImageConf.DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT))
      .withInstanceStopMaxWait(cmd.getSafeOpt(OPT_WITH_INSTANCE_STOP_WAIT).getValueOrDefault(EC2ImageConf.DEFAULT_INSTANCE_STOP_MAX_WAIT))
      .withInstanceTerminationMaxWait(cmd.getSafeOpt(OPT_WITH_INSTANCE_TERM_WAIT).getValueOrDefault(EC2ImageConf.DEFAULT_INSTANCE_TERMINATION_MAX_WAIT));
    
    if (cmd.containsOption(OPT_WITH_RECIPE_ATTRIBUTES, true)) {
      builder.withRecipeAttributes(
          new File(cmd.getOptNotNull(OPT_WITH_RECIPE_ATTRIBUTES).getValue()), 
          Charsets.toCharset(cmd.getOptOrDefault(OPT_WITH_RECIPE_CHARSET, "UTF-8").getValue())
      );
    } else {
      builder.withRecipeAttributes(loadDefaultRecipeAttributes());
    }
    
    WorkflowResult result = builder.build().createImage();
    
    new WorkflowDiagnosticsHelper().withLog(context.getWorflowLog()).displayDiagnostics(result); 
    
    return result.getOutcome() == Outcome.SUCCESS ? StatusCode.SUCCESS : StatusCode.FAILURE;
  }
  
  @Override
  public void displayHelp(CliModuleContext context) {
    OPTIONS.displayHelp(AwsConsts.PROVIDER + " " + CommandDefs.CREATE_IMAGE.getName(), context.getConsole());
     
  }
  
  private String loadDefaultRecipeAttributes() {
    return "";
  }
  
}
