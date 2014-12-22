package org.sapia.corus.client.rest;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.services.security.Permission;


/**
 * A REST resources that allows managing cluster-related information.
 * 
 * @author yduchesne
 *
 */
public class ClusterWriteResource {

  @Path("/clusters/{corus:cluster}/name/{corus:name}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeClusterNameForCluster(RequestContext context) {
    context.getConnector().getCluster().changeCluster(context.getRequest().getValue("corus:name").asString(), ClusterInfo.clustered());
  }

  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/name/{corus:name}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeClusterNameForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getCluster().changeCluster(context.getRequest().getValue("corus:name").asString(), cluster);
  }
  
  @Path("/clusters/{corus:cluster}/repository/role/{corus:role}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeRepoRoleForCluster(RequestContext context) {
    context.getConnector().getCluster().changeCluster(context.getRequest().getValue("corus:role").asString(), ClusterInfo.clustered());
  }

  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/repository/role/{corus:role}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeRepoRoleForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getCluster().changeCluster(context.getRequest().getValue("corus:role").asString(), cluster);
  }
}
