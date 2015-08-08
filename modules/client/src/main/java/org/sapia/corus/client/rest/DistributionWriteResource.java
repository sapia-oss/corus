package org.sapia.corus.client.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Collects;

/**
 * Handles deploy, undeploy.
 * 
 * @author yduchesne
 *
 */
public class DistributionWriteResource extends DeploymentResourceSupport {
  
  public static final int DEFAULT_DEPLOY_BATCH_SIZE = 1;
  
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
    File file = transfer(context);
    try {
      return doDeployDistributionForCluster(context, file);
    } finally {
      file.delete();
    }
  }
  
  private ProgressResult doDeployDistributionForCluster(RequestContext context, File file) throws Exception {
    DeployPreferences prefs = DeployPreferences.newInstance().setExecDeployScripts(
        context.getRequest().getValue("runScripts", "false").asBoolean()
    );
    
    Value checksum = context.getRequest().getValue("checksum-md5");
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
    
    Value batchSize = context.getRequest().getValue("batchSize");
    Value minHosts  = context.getRequest().getValue("minHosts", "1");
    
    if (batchSize.isSet()) {
      List<CorusHost> allHosts = new ArrayList<CorusHost>();
      allHosts.add(context.getConnector().getContext().getServerHost());
      allHosts.addAll(context.getConnector().getContext().getOtherHosts());
      
      int batchSizeValue = batchSize.asInt();
      List<List<CorusHost>> batches;
      if (minHosts.isSet()) {
        int minHostsValue = minHosts.asInt();
        if (allHosts.size() < minHostsValue) {
          batchSizeValue = DEFAULT_DEPLOY_BATCH_SIZE;
        }
      }
      batches = Collects.splitAsLists(allHosts, batchSizeValue);
      ProgressResult toReturn = null;
      for (List<CorusHost> batch : batches) {
        ClusterInfo    cluster = ClusterInfo.clustered().addTargetHosts(batch);
        ProgressResult toAdd   = progress(context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), prefs, cluster));
        if (toReturn == null) {
          toReturn = toAdd;
        } else {
          toReturn.add(toAdd);
        }
        if (toReturn.isError()) {
          return toReturn;
        }
      }
      
      if (toReturn == null) {
        toReturn = new ProgressResult(Collects.arrayToList("No hosts to deploy to"));
      }
      return toReturn;
    } else {
      return progress(context.getConnector().getDeployerFacade().deployDistribution(file.getAbsolutePath(), prefs, ClusterInfo.clustered()));
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
    Value checksum = context.getRequest().getValue("checksum-md5");
    if (checksum.isSet()) {
      prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksum.asString()));
    }
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
    "/clusters/{corus:cluster}/distributions/{corus:name}/{corus:version}/rollback",
    "/clusters/{corus:cluster}/hosts/distributions/{corus:name}/{corus:version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult rollbackDistributionForCluster(RequestContext context) throws Exception {
    String name    = context.getRequest().getValue("corus:name").notNull().asString();
    String version = context.getRequest().getValue("corus:version").notNull().asString();
    
    Value batchSize = context.getRequest().getValue("batchSize");
    Value minHosts  = context.getRequest().getValue("minHosts", "1");
    
    if (batchSize.isSet()) {
      List<CorusHost> allHosts = new ArrayList<CorusHost>();
      allHosts.add(context.getConnector().getContext().getServerHost());
      allHosts.addAll(context.getConnector().getContext().getOtherHosts());
      
      int batchSizeValue = batchSize.asInt();
      List<List<CorusHost>> batches;
      if (minHosts.isSet()) {
        int minHostsValue = minHosts.asInt();
        if (allHosts.size() < minHostsValue) {
          batchSizeValue = DEFAULT_DEPLOY_BATCH_SIZE;
        }
      }
      batches = Collects.splitAsLists(allHosts, batchSizeValue);
      ProgressResult toReturn = null;
      
      for (List<CorusHost> batch : batches) {
        ClusterInfo    cluster = ClusterInfo.clustered().addTargetHosts(batch);
        ProgressResult toAdd   = progress(context.getConnector().getDeployerFacade().rollbackDistribution(name, version, cluster));
        if (toReturn == null) {
          toReturn = toAdd;
        } else {
          toReturn.add(toAdd);
        }
        if (toReturn.isError()) {
          return toReturn;
        }
      }
      
      if (toReturn == null) {
        toReturn = new ProgressResult(Collects.arrayToList("No hosts to deploy to"));
      }
      return toReturn;
    } else {
     return progress(context.getConnector().getDeployerFacade().rollbackDistribution(name, version, ClusterInfo.clustered()));
    }
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions/{corus:name}/{corus:version}/rollback"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public ProgressResult rollbackDistributionForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    String name = context.getRequest().getValue("corus:name").notNull().asString();
    String version = context.getRequest().getValue("corus:version").notNull().asString();
    return progress(context.getConnector().getDeployerFacade().rollbackDistribution(name, version, cluster));
  } 
  
}
