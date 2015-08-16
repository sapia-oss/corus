package org.sapia.corus.client.rest;

import java.util.Arrays;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.rest.async.AsyncParams;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport;
import org.sapia.corus.client.rest.async.AsyncProgressTaskSupport.AsyncProgressTaskContext;
import org.sapia.corus.client.rest.async.ProgressCapableTask;
import org.sapia.corus.client.rest.async.WaitForProcessesKilledTask;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.PortCriteria;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.TimeValue;

/**
 * Handles kill, suspend, restart.
 * 
 * @author yduchesne
 *
 */
public class ProcessWriteResource extends ResourceSupport {

  // --------------------------------------------------------------------------
  // exec

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/exec"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessesForPartition(RequestContext context) throws TooManyProcessInstanceException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doExecProcessesForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/processes/exec",
    "/clusters/{corus:cluster}/hosts/processes/exec"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessesForCluster(RequestContext context) throws TooManyProcessInstanceException {
    return doExecProcessesForCluster(context, ClusterInfo.clustered());
  }
  
  private ProgressResult doExecProcessesForCluster(RequestContext context, ClusterInfo cluster) throws TooManyProcessInstanceException {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    int instances = context.getRequest().getValue("i", "1").asInt();
    return progress(context.getConnector().getProcessorFacade().exec( 
        criteria.build(), 
        instances,
        cluster
    ));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/exec"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessesForHost(RequestContext context) throws TooManyProcessInstanceException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    
    int instances = context.getRequest().getValue("i", "1").asInt();
    return progress(context.getConnector().getProcessorFacade().exec( 
        criteria.build(), 
        instances,
        cluster
    ));
  }
  
  // --------------------------------------------------------------------------
  // kill

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/kill"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult killProcessesForPartition(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doKillProcessesForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/processes/kill",
    "/clusters/{corus:cluster}/hosts/processes/kill"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult killProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    return doKillProcessesForCluster(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/kill"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult killProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doKillProcessesForCluster(context, cluster);
  }
  
  private ProgressResult doKillProcessesForCluster(RequestContext context, ClusterInfo cluster) throws ProcessNotFoundException {
    final ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    
    Value portRangePattern = context.getRequest().getValue("pr");
    Value profile          = context.getRequest().getValue("p");

    if (portRangePattern.isSet()) {
      criteria.ports(PortCriteria.fromLiteral(portRangePattern.asString()));
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d", "*").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v", "*").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()));
      if (profile.isSet()) {
        criteria.profile(profile.asString());
      }
    } else {
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
      criteria.profile(context.getRequest().getValue("p").asString());
    }
    
    return doKill(context, cluster, criteria);
  }
 
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/kill"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult killProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    final ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    
    criteria.pid(ArgMatchers.parse(context.getRequest().getValue("corus:process_id").asString()));
   
    return doKill(context, cluster, criteria);
  }
  
  private ProgressResult doKill(RequestContext context, ClusterInfo cluster, final ProcessCriteria.Builder criteria) {
    
    Value async               = context.getRequest().getValue("async", "false");
    Value check               = context.getRequest().getValue("check", "false");
    final Value timeout       = context.getRequest().getValue("timeout", "240");
    final Value retryInterval = context.getRequest().getValue("retryInterval", "10");
    
    AsyncProgressTaskContext taskContext = new AsyncProgressTaskContext()
      .connectors(context.getConnectors())
      .clustered(cluster);
  
    AsyncProgressTaskSupport task = new AsyncProgressTaskSupport(taskContext) {
      protected ProgressResult doProcess(AsyncParams params) {
        try {
          ProgressResult result = new ProgressResult(Arrays.asList("Proceeding to process kill"));
          params.getConnector().getProcessorFacade().kill(
              criteria.build(), 
              KillPreferences.newInstance(),
              params.getClusterInfo()
          );
          return result;
        } catch (Exception e) {
          throw new IllegalStateException("Could not complete processing of request", e);
        }
      }
    };
    
    if (check.asBoolean()) {
      task.addChainedTask(new Func<ProgressCapableTask, AsyncParams>() {
        @Override
        public ProgressCapableTask call(final AsyncParams params) {
          return new WaitForProcessesKilledTask(criteria.build(), params)
            .setRetryInterval(TimeValue.createSeconds(retryInterval.asInt()))
            .setTimeout(TimeValue.createSeconds(timeout.asInt()));
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

  // --------------------------------------------------------------------------
  // suspend
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/suspend"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void suspendProcessesForPartition(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    doSuspendProcessesForCluster(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/processes/suspend",
    "/clusters/{corus:cluster}/hosts/processes/suspend"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void suspendProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    doSuspendProcessesForCluster(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/suspend"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void suspendProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doSuspendProcessesForCluster(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/suspend"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void suspendProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgMatchers.parse(context.getRequest().getValue("corus:process_id").asString()));
    context.getConnector().getProcessorFacade().suspend(
        criteria.build(), 
        KillPreferences.newInstance().setSuspend(true),
        cluster
    );
  }
  
  private void doSuspendProcessesForCluster(RequestContext context, ClusterInfo cluster) throws ProcessNotFoundException {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    
    Value portRangePattern = context.getRequest().getValue("pr");
    Value profile          = context.getRequest().getValue("p");
    if (portRangePattern.isSet()) {
      criteria.ports(PortCriteria.fromLiteral(portRangePattern.asString()));
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d", "*").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v", "*").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()));
      if (profile.isSet()) {
        criteria.profile(profile.asString());
      }
    } else {
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
      criteria.profile(context.getRequest().getValue("p").asString());
    } 
    
    context.getConnector().getProcessorFacade().suspend(
        criteria.build(), 
        KillPreferences.newInstance().setSuspend(true),
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // resume

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult resumeProcessesForPartition(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    return doResumeProcessesForCluster(context, cluster);
  }

  @Path({
    "/clusters/{corus:cluster}/processes/resume",
    "/clusters/{corus:cluster}/hosts/processes/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult resumeProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    return doResumeProcessesForCluster(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult resumeProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doResumeProcessesForCluster(context, cluster);
   }
  
  private ProgressResult doResumeProcessesForCluster(RequestContext context, ClusterInfo cluster) throws ProcessNotFoundException {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());

    Value portRangePattern = context.getRequest().getValue("pr");
    if (portRangePattern.isSet()) {
      criteria.ports(PortCriteria.fromLiteral(portRangePattern.asString()));
    }
    
    return progress(context.getConnector().getProcessorFacade().resume(
        criteria.build(), 
        cluster
    ));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult resumeProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgMatchers.parse(context.getRequest().getValue("corus:process_id").asString()));
    return progress(context.getConnector().getProcessorFacade().resume(
        criteria.build(), 
        cluster
    ));
  }
  
  // --------------------------------------------------------------------------
  // restart

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult restartProcessesForPartition(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    return doRestartProcessesForCluster(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/processes/restart",
    "/clusters/{corus:cluster}/hosts/processes/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult restartProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    return doRestartProcessesForCluster(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult restartProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doRestartProcessesForCluster(context, cluster);
  }
  
  private ProgressResult doRestartProcessesForCluster(RequestContext context, ClusterInfo cluster) throws ProcessNotFoundException {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    
    Value portRangePattern = context.getRequest().getValue("pr");
    Value profile          = context.getRequest().getValue("p");
    if (portRangePattern.isSet()) {
      criteria.ports(PortCriteria.fromLiteral(portRangePattern.asString()));
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d", "*").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v", "*").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()));
      if (profile.isSet()) {
        criteria.profile(profile.asString());
      }
    } else {
      criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d").asString()));
      criteria.version(ArgMatchers.parse(context.getRequest().getValue("v").asString()));
      criteria.name(ArgMatchers.parse(context.getRequest().getValue("n").asString()));
      criteria.profile(context.getRequest().getValue("p").asString());
    } 
    
    return progress(context.getConnector().getProcessorFacade().restart(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    ));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult restartProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgMatchers.parse(context.getRequest().getValue("corus:process_id").asString()));
    return progress(context.getConnector().getProcessorFacade().restart(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    ));
  }
  
  // --------------------------------------------------------------------------
  // clean

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes/clean"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void cleanProcessesForPartition(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    context.getConnector().getProcessorFacade().clean(targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/processes/clean",
    "/clusters/{corus:cluster}/hosts/processes/clean"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void cleanProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    context.getConnector().getProcessorFacade().clean(cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/clean"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void cleanProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getProcessorFacade().clean(cluster);
  }

}
