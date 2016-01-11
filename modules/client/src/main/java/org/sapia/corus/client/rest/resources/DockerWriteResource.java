package org.sapia.corus.client.rest.resources;

import java.io.File;
import java.util.Arrays;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.DefaultContentLevel;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsyncParams;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport.AsyncProgressTaskContext;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.security.Permission;

/**
 * Deploys/Undeploys Docker objects.
 * 
 * @author yduchesne
 *
 */
public class DockerWriteResource extends DeploymentResourceSupport {

  public static final int DEFAULT_DEPLOY_BATCH_SIZE = 1;
  
  // --------------------------------------------------------------------------
  //  deploy
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult loadImageForPartition(RequestContext context) throws Exception {
    File file = transfer(context, ".tar");
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doLoadImageForCluster(context, file, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/docker/images/{corus:image}",
    "/clusters/{corus:cluster}/hosts/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult loadImageForCluster(RequestContext context) throws Exception {
    File file = transfer(context, ".tar");
    return doLoadImageForCluster(context, file, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult loadImageForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final File  file    = transfer(context, ".tar");
    final DeployPreferences prefs = DeployPreferences.newInstance();
    final String imageName = context.getRequest().getValue("corus:image").notNull().asString();

    Value checksum  = context.getRequest().getValue("checksum-md5");
    Value async     = context.getRequest().getValue("async", "false");
    
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return progress(
              params.getConnector()
                .getDeployerFacade()
                .deployDockerImage(
                    imageName, 
                    file.getAbsolutePath(), prefs, params.getClusterInfo()
                )
          );
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
      @Override
      public void releaseResources() {
        file.delete();
      }
    };

    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }
  
  private ProgressResult doLoadImageForCluster(RequestContext context, final File file, ClusterInfo targets) throws Exception {
    final DeployPreferences prefs = DeployPreferences.newInstance();
    final String imageName = context.getRequest().getValue("corus:image").notNull().asString();
    
    Value checksum = context.getRequest().getValue("checksum-md5");
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
    
    Value batchSize          = context.getRequest().getValue("batchSize", "0");
    Value minHosts           = context.getRequest().getValue("minHosts", "1");
    Value maxErrors          = context.getRequest().getValue("maxErrors", "0");
    Value async              = context.getRequest().getValue("async", "false");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return progress(
              params.getConnector()
                .getDeployerFacade()
                .deployDockerImage(
                    imageName,
                    file.getAbsolutePath(), prefs, params.getClusterInfo()
                )
          );
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
      @Override
      public void releaseResources() {
        file.delete();
      }
    };
   
    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }
  
  // --------------------------------------------------------------------------
  //  undeploy
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/docker/images"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult removeImagesForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService().getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();    
    return doRemoveImagesForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/docker/images",
    "/clusters/{corus:cluster}/hosts/docker/images"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult removeImagesForCluster(RequestContext context) throws Exception {
    return doRemoveImagesForCluster(context, ClusterInfo.clustered());
  }
  
  private ProgressResult doRemoveImagesForCluster(RequestContext context, ClusterInfo targets) throws Exception {

    final ArgMatcher criterion = ArgMatchers.parse(context.getRequest().getValue("n").notNull().asString());
    
    Value batchSize = context.getRequest().getValue("batchSize", "0");
    Value minHosts  = context.getRequest().getValue("minHosts", "1");
    Value maxErrors = context.getRequest().getValue("maxErrors", "0");
    Value async     = context.getRequest().getValue("async", "false");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDockerManagementFacade().removeImages(criterion, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };
  
    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/docker/images"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult removeImagesForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final ArgMatcher criterion = ArgMatchers.parse(context.getRequest().getValue("n").notNull().asString());
    
    Value async = context.getRequest().getValue("async", "false");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDockerManagementFacade().removeImages(criterion, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };

    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }
  
  // --------------------------------------------------------------------------
  // pull
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult pullImageForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService().getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();    
    return doPullImageForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/docker/images/{corus:image}",
    "/clusters/{corus:cluster}/hosts/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult pullImageForCluster(RequestContext context) throws Exception {
    return doPullImageForCluster(context, ClusterInfo.clustered());
  }
  
  private ProgressResult doPullImageForCluster(RequestContext context, ClusterInfo targets) throws Exception {

    final String imageName = context.getRequest().getValue("corus:image").notNull().asString();
    
    Value batchSize = context.getRequest().getValue("batchSize", "0");
    Value minHosts  = context.getRequest().getValue("minHosts", "1");
    Value maxErrors = context.getRequest().getValue("maxErrors", "0");
    Value async     = context.getRequest().getValue("async", "false");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDockerManagementFacade().pullImage(imageName, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };
  
    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/docker/images/{corus:image}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult pullImageForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final String imageName = context.getRequest().getValue("corus:image").notNull().asString();
    
    Value async = context.getRequest().getValue("async", "false");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDockerManagementFacade().pullImage(imageName, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };

    if (async.asBoolean()) {
      String completionToken = context.getAsyncService().registerForExecution(task);
      return new ProgressResult(Arrays.asList("Executing asynchronous operation"))
        .setCompletionToken(completionToken)
        .setStatus(HttpResponseFacade.STATUS_IN_PROGRESS);
    } else {
      task.execute();
      return task.drainAllResults();
    }
  }
}
