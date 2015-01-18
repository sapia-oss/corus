package org.sapia.corus.client.rest;

import java.io.File;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
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
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
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
    "/clusters/{corus:cluster}/exec-configs/{corus:name}",
    "/clusters/{corus:cluster}/hosts/exec-configs/{corus:name}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value config = context.getRequest().getValue("corus:name");
    context.getConnector().getProcessorFacade().undeployExecConfig(config.asString(), cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/{corus:name}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    Value config = context.getRequest().getValue("corus:name");
    context.getConnector().getProcessorFacade().undeployExecConfig(config.asString(), cluster);
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
  public void execProcessConfigForCluster(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.clustered();
    context.getConnector().getProcessorFacade().execConfig(
        context.getRequest().getValue("corus:name").asString(), 
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/{corus:name}/exec"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public void execProcessConfigForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getProcessorFacade().execConfig(
        context.getRequest().getValue("corus:name").asString(), 
        cluster
    );
  }
}
