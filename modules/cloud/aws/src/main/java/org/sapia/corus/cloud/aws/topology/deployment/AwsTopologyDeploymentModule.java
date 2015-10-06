package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.CmdLine;
import org.sapia.console.Options;
import org.sapia.corus.cloud.platform.cli.CliModule;
import org.sapia.corus.cloud.platform.cli.CliModuleContext;
import org.sapia.corus.cloud.platform.workflow.Workflow;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Machine;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.ec2.AmazonEC2Client;

/**
 * Implements topology deployment over AWS.
 * 
 * @author yduchesne
 *
 */
public class AwsTopologyDeploymentModule implements CliModule {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static final String TOPOLOGY_PARAM_IAM_ROLE         = "corus.cloud.instance.role";
  public static final String TOPOLOGY_PARAM_SECURITY_GROUPS  = "corus.cloud.instance.security.groups";
  public static final String TOPOLOGY_PARAM_KEY_NAME         = "corus.cloud.instance.key.name";

  private static final String OPT_WITH_TOPOLOGY_FILE         = "with-topology-file";
  private static final String OPT_WITH_TOPOLOGY_ENV          = "with-topology-env";
  private static final String OPT_WITH_TOPOLOGY_VERSION      = "with-topology-version";
  
  private static final String OPT_WITH_FORMATION_OUTPUT_DIR  = "with-formation-output-dir";
  private static final String OPT_WITH_FORMATION_OUTPUT_FILE = "with-formation-output-file";
  private static final String OPT_WITH_CREATE_STACK          = "with-create-stack";
  
  private static Options OPTIONS = Options.Builder.newInstance()
      .option().name(OPT_WITH_TOPOLOGY_FILE).desc("Specifies the topology descriptor to use as input.").required().mustHaveValue()
      .option().name(OPT_WITH_TOPOLOGY_ENV).desc("Specifies the environment in the context of which to deploy the topology")
        .desc(" (must correspond to one of the environments defined in the topology descriptor).").required().mustHaveValue()
      .option().name(OPT_WITH_TOPOLOGY_VERSION).desc("Allows providing a topology version override (which will override the topology")
        .desc(" version provided in the descriptor).").mustHaveValue()
      .option().name(OPT_WITH_FORMATION_OUTPUT_FILE).desc("Indicates to which file the generated CloudFormation should be written.")
        .desc(" If not specified a file will be automatically created in the temp directory specified by java.io.tmp.dir.")
        .desc(" The file specified is expected to be relative to the directory specified by the ")
        .desc(OPT_WITH_FORMATION_OUTPUT_DIR + " option.").mustHaveValue()
      .option().name(OPT_WITH_FORMATION_OUTPUT_DIR).desc("Indicates to which directory the generated CloudFormation should be written.")
        .mustHaveValue()
      .buildOptions().sortAlphabeticallyRequiredFirst();

  
  @Override
  public WorkflowResult interact(CliModuleContext context, CmdLine cmd) {
    OPTIONS.validate(cmd);
    
    AwsTopologyDeploymentConf conf = new AwsTopologyDeploymentConf();
    
    conf
      .withTopology(Topology.newInstance(new File(cmd.getOptNotNull(OPT_WITH_TOPOLOGY_FILE).getValueNotNull())))
      .withEnvironment(cmd.getOptNotNull(OPT_WITH_TOPOLOGY_ENV).getValueNotNull());
    
    validateTopology(conf);
    
    if (cmd.containsOption(OPT_WITH_TOPOLOGY_VERSION)) {
      conf.withTopologyVersionOverride(cmd.getOpt(OPT_WITH_TOPOLOGY_VERSION).getValueNotNull());
    }
    if (cmd.containsOption(OPT_WITH_FORMATION_OUTPUT_FILE)) {
      conf.withCloudFormationFileName(cmd.getOpt(OPT_WITH_FORMATION_OUTPUT_FILE).getValueNotNull());
    }
    if (cmd.containsOption(OPT_WITH_FORMATION_OUTPUT_DIR)) {
      conf.withCloudFormationOutputDir(new File(cmd.getOpt(OPT_WITH_FORMATION_OUTPUT_DIR).getValueNotNull()));
    }
    conf.withCreateStack(cmd.getSafeOpt(OPT_WITH_CREATE_STACK).getValueOrDefault(true));
    
    if (!conf.isCreateStack()) {
      context.getWorflowLog().warning(OPT_WITH_CREATE_STACK + " option was set to false: CloudFormation stack will not be created");
    }
    
    Workflow<AwsTopologyDeploymentContext> wf = AwsTopologyDeploymentWorkflowFactory.getDefaultWorkFlow(context.getWorflowLog());
    
    AWSCredentials             credentials          = new DefaultAWSCredentialsProviderChain().getCredentials();
    AmazonCloudFormationClient cloudFormationClient = new AmazonCloudFormationClient();
    AmazonEC2Client            ec2Client            = new AmazonEC2Client(credentials);
    wf.execute(new AwsTopologyDeploymentContext(conf, cloudFormationClient, ec2Client));
    return wf.getResult();
  }
  
  @Override
  public void displayHelp(CliModuleContext context) {
    OPTIONS.displayHelp("", context.getConsole());
  }
  
  private void validateTopology(AwsTopologyDeploymentConf conf) {
    List<String> errors = new ArrayList<String>();
    if (!conf.getTopology().existsParam(TOPOLOGY_PARAM_IAM_ROLE)) {
      errors.add(TOPOLOGY_PARAM_IAM_ROLE + " <param> not defined under <topology> element");
    }
    Env env = conf.getTopology().getEnvByName(conf.getEnvironment());
    for (Cluster c : env.getClusters()) {
      for (Machine m : c.getMachines()) {
        if (!m.existsParam(TOPOLOGY_PARAM_KEY_NAME)) {
          errors.add(TOPOLOGY_PARAM_KEY_NAME + " <param> not defined under <machine> element with name: " + m.getName());
        }
        if (!m.existsParam(TOPOLOGY_PARAM_SECURITY_GROUPS)) {
          errors.add(TOPOLOGY_PARAM_SECURITY_GROUPS + " <param> not defined under <machine> element with name: " + m.getName());
        }
      }
    }
    
    if (!errors.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Topology errors found:").append(LINE_SEPARATOR);
      for (String err : errors) {
        sb.append(err).append(LINE_SEPARATOR);
      }
      throw new IllegalArgumentException(sb.toString());
    }
  }
}
