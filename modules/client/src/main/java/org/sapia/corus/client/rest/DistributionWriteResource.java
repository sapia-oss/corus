package org.sapia.corus.client.rest;

import java.io.File;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
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
  public ProgressResult deployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    File        file    = transfer(context);
    DeployPreferences prefs = DeployPreferences.newInstance().setExecDeployScripts(
        context.getRequest().getValue("runScripts", "false").asBoolean()
    );
    try {
      return progress(context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), prefs, cluster));
    } finally {
      file.delete();
    }
  }
  
  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions/revisions/{corus:revId}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult unarchiveDistributionsForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    RevId       revId   = RevId.valueOf(context.getRequest().getValue("corus:revId").notNull().asString());
    return progress(context.getConnector().getDeployerFacade().unarchiveDistributions(revId, cluster));
  }

  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM})
  @Authorized(Permission.DEPLOY)
  public ProgressResult deployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    File        file    = transfer(context);
    DeployPreferences prefs = DeployPreferences.newInstance().setExecDeployScripts(
        context.getRequest().getValue("runScripts", "false").asBoolean()
    );
    try {
      return progress(context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), prefs, cluster));
    } finally {
      file.delete();
    }
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions/revisions/{corus:revId}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult unarchiveDistributionsForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    RevId       revId   = RevId.valueOf(context.getRequest().getValue("corus:revId").notNull().asString());
    return progress(context.getConnector().getDeployerFacade().unarchiveDistributions(revId, cluster));
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
  public ProgressResult undeployDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgMatchers.parse(context.getRequest().getValue("d").asString()))
        .version(ArgMatchers.parse(context.getRequest().getValue("v").asString()))
        .backup(context.getRequest().getValue("backup", "0").asInt())
        .build();
    
    Value               revId = context.getRequest().getValue("rev");
    UndeployPreferences prefs = UndeployPreferences.newInstance();
    if (revId.isSet()) {
      prefs.revId(RevId.valueOf(revId.asString()));
    }
    return progress(context.getConnector().getDeployerFacade().undeployDistribution(criteria, prefs, cluster));
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult undeployDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    DistributionCriteria criteria = DistributionCriteria.builder()
        .name(ArgMatchers.parse(context.getRequest().getValue("d").asString()))
        .version(ArgMatchers.parse(context.getRequest().getValue("v").asString()))
        .backup(context.getRequest().getValue("backup", "0").asInt())
        .build();
    Value               revId = context.getRequest().getValue("rev");
    UndeployPreferences prefs = UndeployPreferences.newInstance();
    if (revId.isSet()) {
      prefs.revId(RevId.valueOf(revId.asString()));
    }
    return progress(context.getConnector().getDeployerFacade().undeployDistribution(criteria, prefs, cluster));
  }  
  
  // --------------------------------------------------------------------------
  // rollback

  @Path({
    "/clusters/{corus:cluster}/distributions/{name}/{version}/rollback",
    "/clusters/{corus:cluster}/hosts/distributions/{name}/{version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult rollbackDistributionForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    String name = context.getRequest().getValue("name").notNull().asString();
    String version = context.getRequest().getValue("version").notNull().asString();
    return progress(context.getConnector().getDeployerFacade().rollbackDistribution(name, version, cluster));
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions/{name}/{version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult rollbackDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    String name = context.getRequest().getValue("name").notNull().asString();
    String version = context.getRequest().getValue("version").notNull().asString();
    return progress(context.getConnector().getDeployerFacade().rollbackDistribution(name, version, cluster));
  } 
  
}
