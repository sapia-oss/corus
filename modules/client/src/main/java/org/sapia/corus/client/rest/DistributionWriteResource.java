package org.sapia.corus.client.rest;

import java.io.File;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles deploy, undeploy.
 * 
 * @author yduchesne
 *
 */
public class DistributionWriteResource extends DeploymentResourceSupport {
  
  // --------------------------------------------------------------------------
  //  deploy
  
  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public void deployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    File        file    = transfer(context);
    try {
      context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), cluster);
    } finally {
      file.delete();
    }
  }

  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public void deployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    File        file    = transfer(context);
    try {
      context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), cluster);
    } finally {
      file.delete();
    }
  }
  
  // --------------------------------------------------------------------------
  // undeploy

  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgFactory.parse(context.getRequest().getValue("d").asString()))
        .version(ArgFactory.parse(context.getRequest().getValue("v").asString()))
        .build();
    context.getConnector().getDeployerFacade().undeployDistribution(criteria, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgFactory.parse(context.getRequest().getValue("d").asString()))
        .version(ArgFactory.parse(context.getRequest().getValue("v").asString()))
        .build();
    context.getConnector().getDeployerFacade().undeployDistribution(criteria, cluster);
  }  
  
}
