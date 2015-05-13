package org.sapia.corus.cloud.aws.topology.deployment;

import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_APP_TAG_NAME;
import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_ORG_TAG_NAME;
import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_VERSION_TAG_NAME;

import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Tag;
import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

public class CreateStack implements WorkflowStep<AwsTopologyDeploymentContext> {
  
  private static final String DESC = "creating cloud formation stack based on generated CloudFormation file";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(final AwsTopologyDeploymentContext context) throws Exception {  
    if (context.getConf().isCreateStack()) {
          
      CreateStackRequest request = new CreateStackRequest()
        .withTemplateBody(Files.toString(context.getGeneratedCloudFormationFile(), Charsets.UTF_8));
      

      for (Map.Entry<String, String> entry : context.getConf().getGlobalTags().entrySet()) {
        Tag t = new Tag();
        t.setKey(entry.getKey());
        t.setValue(entry.getValue());
        request.withTags(t);
      }
      
      String topologyVersion = MoreObjects.firstNonNull(
          context.getConf().getTopologyVersionOverride(), context.getConf().getTopology().getVersion()
      );
      
      request.withTags(createTag(TOPOLOGY_ORG_TAG_NAME, context.getConf().getTopology().getOrg()));
      request.withTags(createTag(TOPOLOGY_APP_TAG_NAME, context.getConf().getTopology().getApplication()));
      request.withTags(createTag(TOPOLOGY_VERSION_TAG_NAME, topologyVersion));
      
      request.withStackName(String.format("%s-%s-%s",
          context.getConf().getTopology().getOrg(),
          context.getConf().getTopology().getApplication(),
          topologyVersion
      ));
      
      request.setOnFailure(OnFailure.DELETE);
      
      CreateStackResult result = context.getCloudFormationClient().createStack(request);
      context.assignStackId(result.getStackId());
    }
  }
  
  private Tag createTag(String key, String value) {
    Tag t = new Tag();
    t.setKey(key);
    t.setValue(value);
    return t;
  }

}
