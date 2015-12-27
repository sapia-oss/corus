package org.sapia.corus.client.rest.resources;

import java.io.File;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.services.database.RevId;
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
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_OCTET_STREAM, ContentTypes.TEXT_XML})
  @Authorized(Permission.DEPLOY)
  public void deployExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    File file = transfer(context, ".xml");
    try {
      context.getConnector().getProcessorFacade().deployExecConfig(file, targets);
    } finally {
      file.delete();
    }
  }

  
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
    File        file    = transfer(context, ".xml");
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
    File        file    = transfer(context, ".xml");
    try {
      context.getConnector().getProcessorFacade().deployExecConfig(file, cluster);
    } finally {
      file.delete();
    }
  }  
  
  // --------------------------------------------------------------------------
  // delete
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void undeployExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value config = context.getRequest().getValue("n").notNull();
    Value backup = context.getRequest().getValue("backup", "0");
    ExecConfigCriteria crit = ExecConfigCriteria.builder()
        .backup(backup.asInt())
        .name(ArgMatchers.parse(config.asString()))
        .build();
    context.getConnector().getProcessorFacade().undeployExecConfig(crit, targets);
  }  
  
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
        .name(ArgMatchers.parse(config.asString()))
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
        .name(ArgMatchers.parse(config.asString()))
        .build();
    context.getConnector().getProcessorFacade().undeployExecConfig(crit, cluster);
  }  
  
  // --------------------------------------------------------------------------
  // enable/disable

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs/enable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void enableExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, true, targets);
  }  
  
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, true, cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs/disable"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void disableExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value config = context.getRequest().getValue("n").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, false, targets);
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(config.asString())).build();
    context.getConnector().getProcessorFacade().setExecConfigEnabled(crit, false, cluster);
  }  
  
  // --------------------------------------------------------------------------
  // archive/unarchive

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archiveExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().archiveExecConfigs(RevId.valueOf(revId.asString()), targets);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs/archive",
    "/clusters/{corus:cluster}/hosts/exec-configs/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archiveExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().archiveExecConfigs(RevId.valueOf(revId.asString()), cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archiveConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().archiveExecConfigs(RevId.valueOf(revId.asString()), cluster);
  }  

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchiveExecConfigForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().unarchiveExecConfigs(RevId.valueOf(revId.asString()), targets);
  }  
  
  
  @Path({
    "/clusters/{corus:cluster}/exec-configs/unarchive",
    "/clusters/{corus:cluster}/hosts/exec-configs/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchiveExecConfigForCluster(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.clustered();
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().unarchiveExecConfigs(RevId.valueOf(revId.asString()), cluster);
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchiveExecConfigForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").notNull().asString());
    Value revId = context.getRequest().getValue("revId").notNull();
    context.getConnector().getProcessorFacade().unarchiveExecConfigs(RevId.valueOf(revId.asString()), cluster);
  }  
  
  // --------------------------------------------------------------------------
  // exec

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs/exec"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.EXECUTE)
  public ProgressResult execProcessConfigForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    Value config = context.getRequest().getValue("corus:name").notNull();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.exact(config.asString())).build();
    return progress(context.getConnector().getProcessorFacade().execConfig(crit, targets));
  }
  
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.exact(config.asString())).build();
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.exact(config.asString())).build();
    return progress(context.getConnector().getProcessorFacade().execConfig(crit, cluster));
  }
}
