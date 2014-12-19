package org.sapia.corus.client.rest;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
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
    "/clusters/{corus:cluster}/tags",
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
  // Restricted methods
  
  private void doAddTags(RequestContext context, ClusterInfo cluster) {
    context.getConnector().getConfigFacade().addTags(
        context.getRequest().getValue("corus:tag").asSet(), 
        cluster
    );
  }
  
  private void doDeleteTag(RequestContext context, ClusterInfo cluster) {
    context.getConnector().getConfigFacade().removeTag(
        context.getRequest().getValue("corus:tag").asString(), 
        cluster
    );
  }
}
