package org.sapia.corus.client.rest;

import java.io.File;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.security.Permission;

/**
 * Allows adding, deleting, and executing execution configurations.
 * 
 * @author yduchesne
 *
 */
public class ExecConfigWriteResource extends DeploymentResourceSupport{
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs",
    "/clusters/{corus:cluster}/hosts/exec-configs"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM, ContentTypes.TEXT_XML})
  @Authorized(Permission.DEPLOY)
  public void deployExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    File        file    = transfer(context);
    try {
      context.getConnector().getProcessorFacade().deployExecConfig(file, cluster);
    } finally {
      file.delete();
    }
  }

  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM, ContentTypes.TEXT_XML})
  @Authorized(Permission.DEPLOY)
  public void deployExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    File        file    = transfer(context);
    try {
      context.getConnector().getProcessorFacade().deployExecConfig(file, cluster);
    } finally {
      file.delete();
    }
  }  
  
  // --------------------------------------------------------------------------
  // delete
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs",
    "/clusters/{corus:cluster}/hosts/exec-configs"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value config = context.getRequest().getValue("n").notNull();
    Value backup = context.getRequest().getValue("backup", "0");
    ExecConfigCriteria crit = ExecConfigCriteria.builder()
        .backup(backup.asInt())
        .name(ArgFactory.parse(config.asString()))
        .build();
    context.getConnector().getProcessorFacade().undeployExecConfig(crit, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value config = context.getRequest().getValue("n").notNull();
    Value backup = context.getRequest().getValue("backup", "0");
    ExecConfigCriteria crit = ExecConfigCriteria.builder()
        .backup(backup.asInt())
        .name(ArgFactory.parse(config.asString()))
        .build();
    context.getConnector().getProcessorFacade().undeployExecConfig(crit, cluster);
  }  
  
  // --------------------------------------------------------------------------
  // enable/disable
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs/enable",
    "/clusters/{corus:cluster}/hosts/exec-configs/enable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void enableExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, true, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/enable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void enableExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, true, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs/disable",
    "/clusters/{corus:cluster}/hosts/exec-configs/disable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void disableExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, false, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/disable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void disableExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, false, cluster);
  }  
  
  // --------------------------------------------------------------------------
  // exec
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs/{corus:name}/exec",
    "/clusters/{corus:cluster}/hosts/exec-configs/{corus:name}/exec"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessConfigForCluster(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value config = context.getRequest().getValue("corus:name").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.exact(config.asString())).build();
    return progress(context.getConnector().getProcessorFacade().execConfig(crit, cluster));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/{corus:name}/exec"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessConfigForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value config = context.getRequest().getValue("corus:name").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.exact(config.asString())).build();
    return progress(context.getConnector().getProcessorFacade().execConfig(crit, cluster));
  }
}
