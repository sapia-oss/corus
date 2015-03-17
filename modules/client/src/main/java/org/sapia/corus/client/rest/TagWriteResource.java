package org.sapia.corus.client.rest;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.security.Permission;

/**
 * Handles addition/deletion of tags.
 * 
 * @author yduchesne
 *
 */
public class TagWriteResource {
  
  // --------------------------------------------------------------------------
  //  add

  @Path({
    "/clusters/{corus:cluster}/tags/{corus:tag}",
    "/clusters/{corus:cluster}/hosts/tags/{corus:tag}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addTagsForCluster(RequestContext context) {
    doAddTags(context, ClusterInfo.clustered());
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/tags/{corus:tag}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addTagsForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doAddTags(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // delete

  @Path({
    "/clusters/{corus:cluster}/tags/{corus:tag}",
    "/clusters/{corus:cluster}/hosts/tags/{corus:tag}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deleteTagForCluster(RequestContext context) {
    doDeleteTag(context, ClusterInfo.clustered());
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/tags/{corus:tag}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deleteTagForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doDeleteTag(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // archive

  @Path({
    "/clusters/{corus:cluster}/tags/archive",
    "/clusters/{corus:cluster}/hosts/tags/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archiveTagForCluster(RequestContext context) {
    doArchiveTags(context, ClusterInfo.clustered());
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/tags/archive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void archiveTagForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doArchiveTags(context, cluster);
  }
  
  @Path({
    "/clusters/{corus:cluster}/tags/unarchive",
    "/clusters/{corus:cluster}/hosts/tags/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchiveTagForCluster(RequestContext context) {
    doUnarchiveTags(context, ClusterInfo.clustered());
  }  
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/tags/unarchive"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void unarchiveTagForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doUnarchiveTags(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private void doAddTags(RequestContext context, ClusterInfo cluster) {
    context.getConnector().getConfigFacade().addTags(
        context.getRequest().getValue("corus:tag").asSet(),
        context.getRequest().getValue("clearExisting", "false").asBoolean(),
        cluster
    );
  }
  
  private void doDeleteTag(RequestContext context, ClusterInfo cluster) {
    context.getConnector().getConfigFacade().removeTag(
        context.getRequest().getValue("corus:tag").asString(), 
        cluster
    );
  }
  
  private void doArchiveTags(RequestContext context, ClusterInfo cluster) {
    String revId = context.getRequest().getValue("revId").notNull().asString();
    context.getConnector().getConfigFacade().archiveTags(
        RevId.valueOf(revId), 
        cluster
    );
  }
  
  private void doUnarchiveTags(RequestContext context, ClusterInfo cluster) {
    String revId = context.getRequest().getValue("revId").notNull().asString();
    context.getConnector().getConfigFacade().unarchiveTags(
        RevId.valueOf(revId), 
        cluster
    );
  }
}
