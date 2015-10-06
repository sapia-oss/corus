package org.sapia.corus.cloud.aws.topology.deployment;

import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_APP_TAG_NAME;
import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_ORG_TAG_NAME;
import static org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConsts.TOPOLOGY_VERSION_TAG_NAME;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.sapia.corus.cloud.platform.settings.Setting;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Tag;
import com.google.common.io.Files;

/**
 * Implements stack creation, based on a given topology.
 * 
 * @author yduchesne
 *
 */
public class CreateStack implements WorkflowStep<AwsTopologyDeploymentContext> {
  
  private static final String DESC = "creating cloud formation stack based on generated CloudFormation file";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(final AwsTopologyDeploymentContext context) throws Exception {  
    if (context.getSettings().getNotNull("isCreateStack").get(Boolean.class)) {
          
      CreateStackRequest request = new CreateStackRequest()
        .withTemplateBody(readCloudFormationFor(context));
      
      for (Map.Entry<String, String> entry : context.getSettings().getNotNull("globalTags").getMapOf(String.class, String.class).entrySet()) {
        Tag t = new Tag();
        t.setKey(entry.getKey());
        t.setValue(entry.getValue());
        request.withTags(t);
      }
      
      Setting  topologyOverride = context.getSettings().get("topologyVersionOverride");
      Topology topology         = context.getSettings().get("topology").get(Topology.class);
      String   topologyVersion  = topologyOverride.isSet() ? topologyOverride.get(String.class) : topology.getVersion();
      
      request.withTags(createTag(TOPOLOGY_ORG_TAG_NAME, topology.getOrg()));
      request.withTags(createTag(TOPOLOGY_APP_TAG_NAME, topology.getApplication()));
      request.withTags(createTag(TOPOLOGY_VERSION_TAG_NAME, topologyVersion));
      
      request.withStackName(String.format("%s-%s-%s",
          topology.getOrg(),
          topology.getApplication(),
          topologyVersion
      ));
      
      request.setOnFailure(OnFailure.DELETE);
      
      CreateStackResult result = context.getCloudFormationClient().createStack(request);
      context.assignStackId(result.getStackId());
    }
  }
  
  protected String readCloudFormationFor(AwsTopologyDeploymentContext context) throws IOException {
    return Files.toString(context.getGeneratedCloudFormationFile(), Charsets.UTF_8);
  }
  
  private Tag createTag(String key, String value) {
    Tag t = new Tag();
    t.setKey(key);
    t.setValue(value);
    return t;
  }

}
