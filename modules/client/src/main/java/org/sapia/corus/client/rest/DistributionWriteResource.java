package org.sapia.corus.client.rest;

import java.io.File;
import java.util.Arrays;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.async.AsyncParams;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport.AsyncProgressTaskContext;
import org.sapia.corus.client.rest.async.PartialDiagnosticTask;
import org.sapia.corus.client.rest.async.ProgressCapableTask;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.TimeValue;

/**
 * Handles deploy, undeploy.
 * 
 * @author yduchesne
 *
 */
public class DistributionWriteResource extends DeploymentResourceSupport {
  
  // ==========================================================================
  
  public static final int DEFAULT_DEPLOY_BATCH_SIZE = 1;
  
  // --------------------------------------------------------------------------
  //  deploy
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult deployDistributionForPartition(RequestContext context) throws Exception {
    File file = transfer(context, "zip");
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doDeployDistributionForCluster(context, file, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult deployDistributionForCluster(RequestContext context) throws Exception {
    File file = transfer(context, ".zip");
    return doDeployDistributionForCluster(context, file, ClusterInfo.clustered());
  }
  
  private ProgressResult doDeployDistributionForCluster(RequestContext context, final File file, ClusterInfo targets) throws Exception {
    final DeployPreferences prefs = DeployPreferences.newInstance().setExecDeployScripts(
        context.getRequest().getValue("runScripts", "false").asBoolean()
    );
    
    Value checksum = context.getRequest().getValue("checksum-md5");
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
    
    Value batchSize          = context.getRequest().getValue("batchSize", "0");
    Value minHosts           = context.getRequest().getValue("minHosts", "1");
    Value maxErrors          = context.getRequest().getValue("maxErrors", "0");
    Value async              = context.getRequest().getValue("async", "false");
    Value runDiags           = context.getRequest().getValue("runDiagnostic", "false");

    final Value diagInterval = context.getRequest().getValue("diagnosticInterval", "10");
    
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
                .deployDistribution(
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
    
    if (runDiags.asBoolean()) {
      task.addChainedTask(new Func<ProgressCapableTask, AsyncParams>() {
        @Override
        public ProgressCapableTask call(AsyncParams params) {
          return new PartialDiagnosticTask(params).setRetryInterval(TimeValue.createSeconds(diagInterval.asInt()));
        }
      });
    }

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
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/distributions/revisions/{corus:revId}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult unarchiveDistributionsForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService().getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doUnarchiveDistributionsForCluster(context, targets);
  }
 
  
  @Path({
    "/clusters/{corus:cluster}/hosts/distributions/revisions/{corus:revId}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult unarchiveDistributionsForCluster(RequestContext context) throws Exception {
    return doUnarchiveDistributionsForCluster(context, ClusterInfo.clustered());
  }
     
  private ProgressResult doUnarchiveDistributionsForCluster(RequestContext context, ClusterInfo targets) throws Exception {

    Value batchSize  = context.getRequest().getValue("batchSize", "0");
    Value minHosts   = context.getRequest().getValue("minHosts", "1");
    Value maxErrors  = context.getRequest().getValue("maxErrors", "0");
    Value async      = context.getRequest().getValue("async", "false");

    final RevId revId        = RevId.valueOf(context.getRequest().getValue("corus:revId").notNull().asString());

    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);
    
    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return progress(params.getConnector().getDeployerFacade().unarchiveDistributions(revId, params.getClusterInfo()));
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
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult deployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final File  file    = transfer(context, ".zip");
    final DeployPreferences prefs = DeployPreferences.newInstance().setExecDeployScripts(
        context.getRequest().getValue("runScripts", "false").asBoolean()
    );
    Value checksum  = context.getRequest().getValue("checksum-md5");
    Value async     = context.getRequest().getValue("async", "false");

    Value       runDiags     = context.getRequest().getValue("runDiagnostic", "false");
    final Value diagInterval = context.getRequest().getValue("diagnosticInterval", "10");
    
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
                .deployDistribution(
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

    if (runDiags.asBoolean()) {
      task.addChainedTask(new Func<ProgressCapableTask, AsyncParams>() {
        @Override
        public ProgressCapableTask call(AsyncParams params) {
          return new PartialDiagnosticTask(params).setRetryInterval(TimeValue.createSeconds(diagInterval.asInt()));
        }
      });
    }

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
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions/revisions/{corus:revId}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult unarchiveDistributionsForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final RevId  revId  = RevId.valueOf(context.getRequest().getValue("corus:revId").notNull().asString());
    final Value  async  = context.getRequest().getValue("async", "false");

    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDeployerFacade().unarchiveDistributions(revId, params.getClusterInfo()));
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
  // undeploy

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult undeployDistributionForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService().getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();    
    return doUndeployDistributionForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult undeployDistributionForCluster(RequestContext context) throws Exception {
    return doUndeployDistributionForCluster(context, ClusterInfo.clustered());
  }
  
  private ProgressResult doUndeployDistributionForCluster(RequestContext context, ClusterInfo targets) throws Exception {

    final DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgMatchers.parse(context.getRequest().getValue("d").asString()))
        .version(ArgMatchers.parse(context.getRequest().getValue("v").asString()))
        .backup(context.getRequest().getValue("backup", "0").asInt())
        .build();
    
    Value batchSize = context.getRequest().getValue("batchSize", "0");
    Value minHosts  = context.getRequest().getValue("minHosts", "1");
    Value maxErrors = context.getRequest().getValue("maxErrors", "0");
    Value async     = context.getRequest().getValue("async", "false");
    Value revId     = context.getRequest().getValue("rev");
    
    final UndeployPreferences prefs = UndeployPreferences.newInstance();
    if (revId.isSet()) {
      prefs.revId(RevId.valueOf(revId.asString()));
    }    
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDeployerFacade().undeployDistribution(criteria, prefs, params.getClusterInfo()));
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
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult undeployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgMatchers.parse(context.getRequest().getValue("d").asString()))
        .version(ArgMatchers.parse(context.getRequest().getValue("v").asString()))
        .backup(context.getRequest().getValue("backup", "0").asInt())
        .build();
    
    Value async = context.getRequest().getValue("async", "false");
    Value revId = context.getRequest().getValue("rev");
    final UndeployPreferences prefs = UndeployPreferences.newInstance();
    if (revId.isSet()) {
      prefs.revId(RevId.valueOf(revId.asString()));
    }
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDeployerFacade().undeployDistribution(criteria, prefs, params.getClusterInfo()));
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
  // rollback

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/distributions/{corus:name}/{corus:version}/rollback",
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult rollbackDistributionForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService().getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();    
    return doRollbackDistributionForCluster(context, targets);
  }
    
  
  @Path({
    "/clusters/{corus:cluster}/distributions/{corus:name}/{corus:version}/rollback",
    "/clusters/{corus:cluster}/hosts/distributions/{corus:name}/{corus:version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult rollbackDistributionForCluster(RequestContext context) throws Exception {
    return doRollbackDistributionForCluster(context, ClusterInfo.clustered());
  }
    
  private ProgressResult doRollbackDistributionForCluster(RequestContext context, ClusterInfo targets) throws Exception {
    
    final String name    = context.getRequest().getValue("corus:name").notNull().asString();
    final String version = context.getRequest().getValue("corus:version").notNull().asString();
    
    Value batchSize          = context.getRequest().getValue("batchSize", "0");
    Value minHosts           = context.getRequest().getValue("minHosts", "1");
    Value maxErrors          = context.getRequest().getValue("maxErrors", "0");
    Value async              = context.getRequest().getValue("async", "false");
    Value runDiags           = context.getRequest().getValue("runDiagnostic", "false");
    final Value diagInterval = context.getRequest().getValue("diagnosticInterval", "10");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .batchSize(batchSize.asInt())
      .minHosts(minHosts.asInt())
      .maxErrors(maxErrors.asInt())
      .connectors(context.getConnectors())
      .clustered(targets);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDeployerFacade().rollbackDistribution(name, version, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };
    
    if (runDiags.asBoolean()) {
      task.addChainedTask(new Func<ProgressCapableTask, AsyncParams>() {
        @Override
        public ProgressCapableTask call(AsyncParams params) {
          return new PartialDiagnosticTask(params).setRetryInterval(TimeValue.createSeconds(diagInterval.asInt()));
        }
      });
    }
  
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
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions/{corus:name}/{corus:version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  @DefaultContentLevel(ContentLevel.MINIMAL)
  public ProgressResult rollbackDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster  = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final String name    = context.getRequest().getValue("corus:name").notNull().asString();
    final String version = context.getRequest().getValue("corus:version").notNull().asString();

    Value async              = context.getRequest().getValue("async", "false");
    Value runDiags           = context.getRequest().getValue("runDiagnostic", "false");
    final Value diagInterval = context.getRequest().getValue("diagnosticInterval", "10");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);

    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          return  progress(params.getConnector().getDeployerFacade().rollbackDistribution(name, version, params.getClusterInfo()));
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };
    
    if (runDiags.asBoolean()) {
      task.addChainedTask(new Func<ProgressCapableTask, AsyncParams>() {
        @Override
        public ProgressCapableTask call(AsyncParams params) {
          return new PartialDiagnosticTask(params).setRetryInterval(TimeValue.createSeconds(diagInterval.asInt()));
        }
      });
    }
  
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
