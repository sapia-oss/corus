package org.sapia.corus.client.rest;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles kill, suspend, restart.
 * 
 * @author yduchesne
 *
 */
public class ProcessWriteResource {

  // --------------------------------------------------------------------------
  // exec
  
  @Path({
    "/clusters/{corus:cluster}/processes/exec",
    "/clusters/{corus:cluster}/hosts/processes/exec"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void execProcessesForCluster(RequestContext context) throws TooManyProcessInstanceException {
    ClusterInfo cluster = ClusterInfo.clustered();
    if (context.getRequest().getValue("e").isSet()) {
      context.getConnector().getProcessorFacade().execConfig(
          context.getRequest().getValue("e").asString(), 
          cluster
      );
    } else {
      ProcessCriteria.Builder criteria = ProcessCriteria.builder();
      criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
      criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
      criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
      criteria.profile(context.getRequest().getValue("p").asString());
      int instances = context.getRequest().getValue("i", "1").asInt();
      context.getConnector().getProcessorFacade().exec( 
          criteria.build(), 
          instances,
          cluster
      );
    }
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/exec"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void execProcessesForHost(RequestContext context) throws TooManyProcessInstanceException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    if (context.getRequest().getValue("e").isSet()) {
      context.getConnector().getProcessorFacade().execConfig(
          context.getRequest().getValue("e").asString(), 
          cluster
      );
    } else {
      ProcessCriteria.Builder criteria = ProcessCriteria.builder();
      criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
      criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
      criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
      criteria.profile(context.getRequest().getValue("p").asString());
      int instances = context.getRequest().getValue("i", "1").asInt();
      context.getConnector().getProcessorFacade().exec( 
          criteria.build(), 
          instances,
          cluster
      );
    }
  }
  
  // --------------------------------------------------------------------------
  // kill
  
  @Path({
    "/clusters/{corus:cluster}/processes/kill",
    "/clusters/{corus:cluster}/hosts/processes/kill"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void killProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().kill(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/kill"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void killProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().kill(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }
 
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/kill"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void killProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgFactory.parse(context.getRequest().getValue("corus:process_id").asString()));
    context.getConnector().getProcessorFacade().kill(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }

  // --------------------------------------------------------------------------
  // suspend
  
  @Path({
    "/clusters/{corus:cluster}/processes/suspend",
    "/clusters/{corus:cluster}/hosts/processes/suspend"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void suspendProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().suspend(
        criteria.build(), 
        KillPreferences.newInstance().setSuspend(true),
        cluster
    );
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
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().suspend(
        criteria.build(), 
        KillPreferences.newInstance().setSuspend(true),
        cluster
    );
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
    criteria.pid(ArgFactory.parse(context.getRequest().getValue("corus:process_id").asString()));
    context.getConnector().getProcessorFacade().suspend(
        criteria.build(), 
        KillPreferences.newInstance().setSuspend(true),
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // resume
  
  @Path({
    "/clusters/{corus:cluster}/processes/resume",
    "/clusters/{corus:cluster}/hosts/processes/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void resumeProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().resume(
        criteria.build(), 
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void resumeProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().resume(
        criteria.build(), 
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/resume"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void resumeProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgFactory.parse(context.getRequest().getValue("corus:process_id").asString()));
    context.getConnector().getProcessorFacade().resume(
        criteria.build(), 
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // restart
  
  @Path({
    "/clusters/{corus:cluster}/processes/restart",
    "/clusters/{corus:cluster}/hosts/processes/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void restartProcessesForCluster(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.clustered();
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().restart(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void restartProcessesForHost(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n").asString()));
    criteria.profile(context.getRequest().getValue("p").asString());
    context.getConnector().getProcessorFacade().restart(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}/restart"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void restartProcessForId(RequestContext context) throws ProcessNotFoundException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgFactory.parse(context.getRequest().getValue("corus:process_id").asString()));
    context.getConnector().getProcessorFacade().restart(
        criteria.build(), 
        KillPreferences.newInstance(),
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // clean
  
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
