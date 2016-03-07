package org.sapia.corus.cloud.aws.topology.deployment.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentContext;
import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.domain.DeploymentJournalEntry;
import org.sapia.corus.cloud.platform.domain.DeploymentJournalEntry.DeploymentStatus;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.rest.CorusRestClient;
import org.sapia.corus.cloud.platform.rest.JSONValue;
import org.sapia.corus.cloud.platform.rest.helper.AsyncResponseHelper;
import org.sapia.corus.cloud.platform.settings.Setting;
import org.sapia.corus.cloud.platform.util.Input;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.platform.workflow.exceptions.AbortedDeploymentException;

import com.google.common.base.Function;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public abstract class DeploymentStepSupport implements WorkflowStep<AwsTopologyDeploymentContext> {
  
  private static final int DEFAULT_MAX_ERRORS = 0;
  private static final int DEFAULT_MIN_HOSTS  = 1;
  
  private String description;
  private Function<AwsTopologyDeploymentContext, List<Input>> inputFunc;
  private Function<CorusInstance, String> resourceFunc;
  
  public DeploymentStepSupport(String desc, 
      Function<AwsTopologyDeploymentContext, List<Input>> inputFunc,
      Function<CorusInstance, String> resourceFunc) {
    this.description  = desc;
    this.inputFunc    = inputFunc;
    this.resourceFunc = resourceFunc;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
    List<Input> inputList = inputFunc.apply(context);
    if (inputList.isEmpty()) {
      context.getLog().warning("Nothing to deploy");
    } else {
      doExecute(context, inputList);
    }
  }

  private void doExecute(AwsTopologyDeploymentContext context, List<Input> inputList) throws Exception {
    
    for (CorusInstance instance : context.getCorusInstances()) {
      context.getLog().info("Performing deployment to Corus instance: %s", instance);
      for (Input input : inputList) {
        context.getLog().info("Deploying: %s", input.getInfo());
        try (InputStream is = input.getInputStream()) {
          int status = doDeploy(context, input.getInfo(), is, instance);
          if (status == CorusRestClient.STATUS_SERVER_ERROR) {
            throw new AbortedDeploymentException(String.format("Aborting deployment. Got error deploying %s on %s", input.getInfo(), instance));
          }
        }
      }
    }
  }
  
  private int doDeploy(final AwsTopologyDeploymentContext context, String deploymentInfo, InputStream is, CorusInstance instance) throws IOException {    
    CorusRestClient client = context.getRestClientFactory().getClient(
        instance.getAddress(), 
        context.getSettings().get("corusCredentials").get(CorusCredentials.class)
    );
    
    try{
      return doDeployWithClient(context, client, deploymentInfo, is, instance);
    } finally {
      client.close();
    }

  }
  
  private int doDeployWithClient(
      final AwsTopologyDeploymentContext context, 
      CorusRestClient client,
      String deploymentInfo, 
      InputStream is, 
      CorusInstance instance) throws IOException {    

    WebTarget resource = client.resource(resourceFunc.apply(instance))
        .queryParam("async", true);
    
    Setting batchSizeSetting = context.getSettings().get("corusRestBatchSize");
    if (batchSizeSetting.isSet()) {
      int batchSize = batchSizeSetting.get(Integer.class).intValue();
      
      if (batchSize > 0) {
        int maxErrors = context.getSettings().get("corusRestMaxErrors", DEFAULT_MAX_ERRORS).get(Integer.class).intValue();
        int minHosts  = context.getSettings().get("corusRestMinHosts", DEFAULT_MIN_HOSTS).get(Integer.class).intValue();
        resource
          .queryParam("batchSize", batchSize)
          .queryParam("maxErrors", maxErrors)
          .queryParam("minHosts", minHosts);
      }
    }
    
    JSONObject response = resource
        .request()
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .put(Entity.entity(is, MediaType.APPLICATION_OCTET_STREAM_TYPE))
          .readEntity(JSONValue.class).asObject();
    
    int status = response.getInt("status");
    
    if (status == CorusRestClient.STATUS_SERVER_ERROR) {
      if  (response.containsKey("feedback")) {
        JSONArray messages = response.getJSONArray("feedback");
        context.getLog().error("Error during deployment of %s to %s. Feedback: ", deploymentInfo, instance);
        for (int i = 0; i < messages.size(); i++) {
          context.getLog().error(messages.getString(i));
        }
      } else {
        context.getLog().error("Error during deployment of %s to %s", deploymentInfo, instance);
      }
      return status;
    } else {
      String completionToken = response.getString("completionToken");
      TimeMeasure pollingInterval = context.getSettings().getNotNull("pollingInterval").get(TimeMeasure.class);
      AsyncResponseHelper helper = new AsyncResponseHelper(client, pollingInterval, completionToken);
      
      try {
        status = helper.complete();
      } catch (InterruptedException e) {
        throw new IllegalStateException("Thread interrupted while performing deployment");
      }
      
      switch (status) {
        case CorusRestClient.STATUS_OK:
          String okMsg = String.format("Deployment of %s completed successfully on: %s)", deploymentInfo, instance);
          context.getLog().info(okMsg);
          context.getDeploymentJournal().addEntry(
              new DeploymentJournalEntry(
                  instance, 
                  DeploymentStatus.SUCCESS, 
                  okMsg
              )
          );
          return status;
        case CorusRestClient.STATUS_PARTIAL_SUCCESS:
          String partialMsg = String.format("Deployment of %s completed partially on: %s (check logs for details)", deploymentInfo, instance);
          context.getLog().info(partialMsg);
          context.getDeploymentJournal().addEntry(
              new DeploymentJournalEntry(
                  instance, 
                  DeploymentStatus.PARTIAL_SUCCESS, 
                  partialMsg
              )
          );
          context.getLog().warning(partialMsg);
          return status;
        case CorusRestClient.STATUS_SERVER_ERROR:
          String errorMsg = String.format("Deployment of %s failed on: %s (check logs for details)", deploymentInfo, instance);
          context.getLog().info(errorMsg);
          context.getDeploymentJournal().addEntry(
              new DeploymentJournalEntry(
                  instance, 
                  DeploymentStatus.FAILURE, 
                  errorMsg
              )
          );
          context.getLog().error(errorMsg);
          return status;
        default:
          throw new IllegalStateException("Unexpected status returned: " + status);
      }
    }      
  }
}
