package org.sapia.corus.cloud.aws.image.creation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import net.sf.json.JSONObject;

import org.sapia.console.CmdLine;
import org.sapia.console.Options;
import org.sapia.corus.cloud.platform.cli.CliModule;
import org.sapia.corus.cloud.platform.cli.CliModuleContext;
import org.sapia.corus.cloud.platform.cli.CommandDefs;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.Workflow;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

/**
 * Executes the {@link CommandDefs#CREATE_IMAGE} command in the context of the AWS provider.
 * 
 * @author yduchesne
 *
 */
public class AwsImageCreationModule implements CliModule {
  
  private static final String OPT_WITH_COOKBOOKS         = "with-cookbooks";
  private static final String OPT_WITH_AWS_CLI           = "with-aws-cli";
  private static final String OPT_WITH_RECIPE_ATTRIBUTES = "with-recipe-attributes";
  private static final String OPT_WITH_IAM_ROLE          = "with-iam-role";
  private static final String OPT_WITH_IMAGE_ID          = "with-image-id";
  private static final String OPT_WITH_KET_PAIR          = "with-keypair";
  private static final String OPT_WITH_REGION            = "with-region";
  private static final String OPT_WITH_SECURITY_GROUPS   = "with-security-groups";
  private static final String OPT_WITH_SUBNET            = "with-subnet";
  private static final String OPT_WITH_YUM_UPDATE        = "with-yum-update";
  private static final String OPT_WITH_IMG_NAME_PREFIX   = "with-image-name-prefix";
  private static final String OPT_WITH_CORUS_PORT        = "with-corus-port";
  private static final String OPT_WITH_USERDATA_INSERT   = "with-userdata-insert";
  private static final String OPT_WITH_USERDATA_APPEND   = "with-userdata-append";
  private static final String OPT_WITH_RECIPE_CORUS_VER  = "with-recipe-corus-version";
  private static final String OPT_WITH_RECIPE_CORUS_URL  = "with-recipe-corus-url";
  private static final String OPT_WITH_RECIPE_JDK_VER    = "with-recipe-jdk-version";
  
  
  private static final String OPT_WITH_CORUS_INSTALL_WAIT = "with-corus-install-wait";
  private static final String OPT_WITH_INSTANCE_RUN_WAIT  = "with-instance-run-wait";
  private static final String OPT_WITH_INSTANCE_STOP_WAIT = "with-instance-stop-wait";
  private static final String OPT_WITH_INSTANCE_TERM_WAIT = "with-instance-term-wait";
  private static final String OPT_WITH_IMG_CREATION_WAIT  = "with-image-creation-wait";
  
  
  public static Options OPTIONS = Options.Builder.newInstance()
      .option().name(OPT_WITH_AWS_CLI).desc("Indicates if the Python-based AWS command-line should be installed on the instance. Defaults to true.").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_CORUS_VER).desc("When using the built-in JSON recipe attributes, indicates the Corus version to use.")
        .desc(" Uses the version specified in the cookbook itself by default.").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_CORUS_URL).desc("When using the built-in JSON recipe attributes, provides the URL to use for downloading the Corus package.")
        .desc(" Uses the URL specified in the cookbook itself by default.").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_JDK_VER).desc("When using the built-in JSON recipe attributes, specifies which JDK version to install.")
        .desc(" Uses the version specified in the cookbook itself by default (that version corresponds to Java 8).").mustHaveValue()
      .option().name(OPT_WITH_USERDATA_INSERT).desc("Expects the path to a  user data file containing shell")
        .desc(" commands to be executed BEFORE the built-in shell command sequence.").mustHaveValue()
      .option().name(OPT_WITH_USERDATA_APPEND).desc("Expects the path to a  user data file containing shell")
        .desc(" commands to be executed AFTER the built-in shell command sequence.").mustHaveValue()
      .option().name(OPT_WITH_COOKBOOKS).desc("A comma-delimited list of chef cookbooks to install on the image.").mustHaveValue()
      .option().name(OPT_WITH_RECIPE_ATTRIBUTES).desc("A path to a JSON file holding Chef recipe attributes, and the run-list to execute.").mustHaveValue()
      .option().name(OPT_WITH_IAM_ROLE).desc("The name of the IAM role under which to start the instance from which an image will be created.").mustHaveValue()
      .option().name(OPT_WITH_IMAGE_ID).desc("The Amazon image ID corresponding to the image from which to create a new image.").mustHaveValue().required()
      .option().name(OPT_WITH_KET_PAIR).desc("The name of the keypair to associate the instance from which an image will be created.").mustHaveValue().required()
      .option().name(OPT_WITH_REGION).desc("The identifier of the AWS region under which to start the instance used for image creation - defaults to: " 
          + ImageCreationConf.DEFAULT_REGION + ".").mustHaveValue()
      .option().name(OPT_WITH_SECURITY_GROUPS).desc("A comma-delimited list of security group IDs under which to run the instance used for image creation.")
        .mustHaveValue().required()
      .option().name(OPT_WITH_SUBNET).desc("The ID of the subnet in which the instance used for image creation should be started." 
        + " Note that the provided security group IDs must be associated to the subnet being specified.").mustHaveValue().required()
      .option().name(OPT_WITH_YUM_UPDATE).desc("Indicates if a yum update should be peformed on the instance used for image creation (true by default)").mustHaveValue()
      .option().name(OPT_WITH_IMG_NAME_PREFIX).desc("Specifies what image name prefix to use - defaults to " + ImageCreationConf.DEFAULT_IMAGE_PREFIX 
        + " A suffix is automatically appended which corresponds to the current date/time.").mustHaveValue()
      .option().name(OPT_WITH_CORUS_PORT).desc("Indicates the port of the Corus server that will be installed. Used for pinging Corus in order to"
          + " determine installation completion - defaults to " + ImageCreationConf.DEFAULT_CORUS_PORT + ".").mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_RUN_WAIT).desc("Max amount of time to wait for the instance (from which an image will be created) to stop " 
          + " - defaults to " + ImageCreationConf.DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT + " minutes.").mustHaveValue()
      .option().name(OPT_WITH_CORUS_INSTALL_WAIT).desc("Max amount of time to wait for Corus installation completion - defaults to " 
          + ImageCreationConf.DEFAULT_CORUS_INSTALL_MAX_WAIT + " minutes.").mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_STOP_WAIT).desc("Max mount of time to wait for the instance on which Corus was installed to stop" 
          + " (before an image is created from it). Defaults to " +  ImageCreationConf.DEFAULT_INSTANCE_STOP_MAX_WAIT + " minutes.").mustHaveValue()
      .option().name(OPT_WITH_IMG_CREATION_WAIT).desc("Max amount of time wait for image creation (from the running Corus instance) - defaults"
          + " to " + ImageCreationConf.DEFAULT_IMG_CREATION_MAX_WAIT + " minutes.").mustHaveValue()
      .option().name(OPT_WITH_INSTANCE_TERM_WAIT).desc("Max amount of time to wait for the instance used to create the image to be terminated"
          + " - defaults to " + ImageCreationConf.DEFAULT_INSTANCE_TERMINATION_MAX_WAIT + " minutes.").mustHaveValue()
      .buildOptions().sortAlphabeticallyRequiredFirst();
  
  @Override
  public WorkflowResult interact(CliModuleContext context, CmdLine cmd) {
    OPTIONS.validate(cmd);

    ImageCreationConf conf = new ImageCreationConf();
    
    conf
      .withImageId(cmd.getOptNotNull(OPT_WITH_IMAGE_ID).getValue())
      .withIamRole(cmd.getOptNotNull(OPT_WITH_IAM_ROLE).getValue())
      .withCookbooks(Arrays.asList(cmd.getOptOrDefault(OPT_WITH_COOKBOOKS, "tar,java,corus").getSplitValue()))
      .withKeypair(cmd.getOptNotNull(OPT_WITH_KET_PAIR).getValue())
      .withRegion(cmd.getSafeOpt(OPT_WITH_REGION).getValueOrDefault(ImageCreationConf.DEFAULT_REGION))
      .withSecurityGroups(Arrays.asList(cmd.getOptNotNull(OPT_WITH_SECURITY_GROUPS).getSplitValue()))
      .withSubnetId(cmd.getOptNotNull(OPT_WITH_SUBNET).getValue())
      .withYumUpdate(cmd.getSafeOpt(OPT_WITH_YUM_UPDATE).getValueOrDefault(true))
      .withAwsCliInstall(cmd.getSafeOpt(OPT_WITH_AWS_CLI).getValueOrDefault(true))
      .withCorusImageNamePrefix(cmd.getOptOrDefault(OPT_WITH_IMG_NAME_PREFIX, ImageCreationConf.DEFAULT_IMAGE_PREFIX).getValue())
      .withCorusPort(cmd.getSafeOpt(OPT_WITH_CORUS_PORT).getValueOrDefault(ImageCreationConf.DEFAULT_CORUS_PORT))
      .withCorusIntallCheckRetry(
          RetryCriteria.forMaxDuration(
              TimeMeasure.forSeconds(ImageCreationConf.POLLING_INTERVAL_SECONDS),
              TimeMeasure.forMinutes(
                cmd
                  .getSafeOpt(OPT_WITH_CORUS_INSTALL_WAIT)
                  .getValueOrDefault(ImageCreationConf.DEFAULT_CORUS_INSTALL_MAX_WAIT)
              )
          )
      )
      .withImgCreationCheckRetry(
          RetryCriteria.forMaxDuration(
              TimeMeasure.forSeconds(ImageCreationConf.POLLING_INTERVAL_SECONDS),
              TimeMeasure.forMinutes(
                cmd
                  .getSafeOpt(OPT_WITH_IMG_CREATION_WAIT)
                  .getValueOrDefault(ImageCreationConf.DEFAULT_IMG_CREATION_MAX_WAIT)
              )
          )
      )
      .withInstanceRunCheckRetry(
          RetryCriteria.forMaxDuration(
              TimeMeasure.forSeconds(ImageCreationConf.POLLING_INTERVAL_SECONDS),
              TimeMeasure.forMinutes(
                cmd
                  .getSafeOpt(OPT_WITH_INSTANCE_RUN_WAIT)
                  .getValueOrDefault(ImageCreationConf.DEFAULT_INSTANCE_RUN_CHECK_MAX_WAIT)
              )
          )
      )
      .withInstanceStopCheckRetry(
          RetryCriteria.forMaxDuration(
              TimeMeasure.forSeconds(ImageCreationConf.POLLING_INTERVAL_SECONDS),
              TimeMeasure.forMinutes(
                cmd
                  .getSafeOpt(OPT_WITH_INSTANCE_STOP_WAIT)
                  .getValueOrDefault(ImageCreationConf.DEFAULT_INSTANCE_STOP_MAX_WAIT)
              )
          )
      )
      .withInstanceTerminatedCheckRetry(
          RetryCriteria.forMaxDuration(
              TimeMeasure.forSeconds(ImageCreationConf.POLLING_INTERVAL_SECONDS),
              TimeMeasure.forMinutes(
                cmd
                  .getSafeOpt(OPT_WITH_INSTANCE_TERM_WAIT)
                  .getValueOrDefault(ImageCreationConf.DEFAULT_INSTANCE_TERMINATION_MAX_WAIT)
              )
          )
      );       
    
    if (cmd.containsOption(OPT_WITH_RECIPE_ATTRIBUTES, true)) {
      conf.withRecipeAttributes(loadRecipeAttributes(
          new File(cmd.getOptNotNull(OPT_WITH_RECIPE_ATTRIBUTES).getValue())
      ));
    } else {
      conf.withRecipeAttributes(loadDefaultRecipeAttributes(
          Optional.fromNullable(cmd.getSafeOpt(OPT_WITH_RECIPE_CORUS_VER).getValue()),
          Optional.fromNullable(cmd.getSafeOpt(OPT_WITH_RECIPE_CORUS_URL).getValue()),
          Optional.fromNullable(cmd.getSafeOpt(OPT_WITH_RECIPE_JDK_VER).getValue())
      ));
    }
    
    AmazonEC2Client     ec2Client = new AmazonEC2Client(conf.getAwsCredentials());
    
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
  
  
  
  
  @Override
  public void displayHelp(CliModuleContext context) {
    OPTIONS.displayHelp("", context.getConsole());
  }
  
  // --------------------------------------------------------------------------
  // Restricted
 
  private String loadRecipeAttributes(File toLoad) {
    InputStream is = null;
    
    try {
      is = new FileInputStream(toLoad);
      String     jsonContent = CharStreams.toString(new InputStreamReader(is));
      JSONObject json        = JSONObject.fromObject(jsonContent);
      return json.toString();
    } catch (IOException e) {
      throw new IllegalStateException("I/O error caught trying to load recipe attributes file: " + toLoad.getAbsolutePath());
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // noop
      }
    }
  }
  
  private String loadDefaultRecipeAttributes(
      Optional<String> corusVersion, 
      Optional<String> corusDownloadUrl,
      Optional<String> jdkVersion) {
    InputStream is = getClass().getResourceAsStream("default_recipe_attributes.json");
    Preconditions.checkState(is != null, "Could not find default_recipe_attributes.json");
    try {
      String     jsonContent = CharStreams.toString(new InputStreamReader(is));
      JSONObject json        = JSONObject.fromObject(jsonContent);
      JSONObject corus       = json.getJSONObject("corus");
      JSONObject java        = json.getJSONObject("java");
    
      Preconditions.checkState(corus != null && !corus.isNullObject(), "'corus' field not found");
      Preconditions.checkState(java != null && !java.isNullObject(), "'java' field not found");

      if (corusVersion.isPresent()) {
        corus.element("version", corusVersion.get());
      }
      if (corusDownloadUrl.isPresent()) {
        corus.element("archive_download_url", corusDownloadUrl.get());
      }
      if (jdkVersion.isPresent()) {
        corus.element("jdk_version", jdkVersion.get());
      }
      return json.toString();
      
    } catch (IOException e) {
      throw new IllegalStateException("Caught I/O exception while trying to load default recipe attributes", e);
    } finally {
      try {
        is.close();
      } catch (Exception e) {
        // noop
      }
    }
  }
  
}
