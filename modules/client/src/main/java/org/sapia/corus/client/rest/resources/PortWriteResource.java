package org.sapia.corus.client.rest.resources;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles addition/deletion of port ranges.
 * 
 * @author yduchesne
 *
 */
public class PortWriteResource {

  // --------------------------------------------------------------------------
  //  add

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addPortRangeForPartition(RequestContext context)
      throws PortRangeInvalidException, PortRangeConflictException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    doAddPortRange(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/ports/ranges/{corus:rangeName}",
    "/clusters/{corus:cluster}/hosts/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addPortRangeForCluster(RequestContext context)
      throws PortRangeInvalidException, PortRangeConflictException {
    doAddPortRange(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addPortRangeForHost(RequestContext context)
      throws PortRangeInvalidException, PortRangeConflictException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doAddPortRange(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  //  delete

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deletePortRangeForPartition(RequestContext context)
      throws PortActiveException {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    doDeletePortRange(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/ports/ranges/{corus:rangeName}",
    "/clusters/{corus:cluster}/hosts/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deletePortRangeForCluster(RequestContext context)
      throws PortActiveException {
    doDeletePortRange(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/ports/ranges/{corus:rangeName}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deletePortRangeForHost(RequestContext context)
      throws PortActiveException {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doDeletePortRange(context, cluster);
  }
    
  // --------------------------------------------------------------------------
  // archive/unarchive 

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/ports/ranges/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archivePortRangeForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    doArchivePortRange(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/ports/ranges/archive",
    "/clusters/{corus:cluster}/hosts/ports/ranges/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archivePortRangeForCluster(RequestContext context) {
    doArchivePortRange(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/ports/ranges/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archivePortRangeForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doArchivePortRange(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/ports/ranges/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchivePortRangeForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    doUnarchivePortRange(context, targets);
  }
  
  @Path({
    "/clusters/{corus:cluster}/ports/ranges/unarchive",
    "/clusters/{corus:cluster}/hosts/ports/ranges/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchivePortRangeForCluster(RequestContext context) {
    doUnarchivePortRange(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/ports/ranges/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchivePortRangeForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doUnarchivePortRange(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private void doAddPortRange(RequestContext context, ClusterInfo cluster) 
      throws PortRangeInvalidException, PortRangeConflictException {
    String name = context.getRequest().getValue("corus:rangeName").notNull().asString();
    int    min  = context.getRequest().getValue("min").notNull().asInt();
    int    max  = context.getRequest().getValue("max").notNull().asInt();
    context.getConnector().getPortManagementFacade().addPortRange(name, min, max, cluster);
  }
  
  private void doDeletePortRange(RequestContext context, ClusterInfo cluster) 
      throws PortActiveException {
    String name   = context.getRequest().getValue("corus:rangeName").notNull().asString();
    boolean force = context.getRequest().getValue("force", "false").asBoolean();
    context.getConnector().getPortManagementFacade().removePortRange(name, force, cluster);
  }
  
  private void doArchivePortRange(RequestContext context, ClusterInfo cluster) {
    String revId   = context.getRequest().getValue("revId").notNull().asString();
    context.getConnector().getPortManagementFacade().archive(RevId.valueOf(revId), cluster);
  }
  
  private void doUnarchivePortRange(RequestContext context, ClusterInfo cluster) {
    String revId   = context.getRequest().getValue("revId").notNull().asString();
    context.getConnector().getPortManagementFacade().unarchive(RevId.valueOf(revId), cluster);
  }
}