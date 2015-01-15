package org.sapia.corus.client.rest;

import java.io.StringWriter;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.security.Permission;

/**
 * A REST resources that allows viewing cluster-related information.
 * 
 * @author yduchesne
 *
 */
public class ClusterResource {
  
  private static final String CLIENT = "client";
  private static final String SERVER = "server";
  private static final String NONE   = "none";


  @Path("/clusters")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getClusters(RequestContext context) {
    StringWriter     output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream
      .beginArray()
        .beginObject()
          .field("name").value(context.getConnector().getContext().getDomain())
        .endObject()
      .endArray();
    return output.toString();   
  }
  
  @Path("/clusters/{corus:cluster}/hosts")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getHostsForCluster(RequestContext context) {
    StringWriter     output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    streamHost(context, stream, context.getConnector().getContext().getServerHost());
    for (CorusHost host : context.getConnector().getContext().getOtherHosts()) {
      streamHost(context, stream, host);
    }
    stream.endArray();
    return output.toString();   
  }
  
  @Path({
    "/clusters/{corus:cluster}/name/{corus:name}",
    "/clusters/{corus:cluster}/hosts/name/{corus:name}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeClusterNameForCluster(RequestContext context) {
    context.getConnector().getCluster().changeCluster(
        context.getRequest().getValue("corus:name").asString(), 
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/name/{corus:name}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeClusterNameForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getCluster().changeCluster(
        context.getRequest().getValue("corus:name").asString(), 
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/repo/role/{corus:role}",
    "/clusters/{corus:cluster}/hosts/repo/role/{corus:role}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeRepoRoleForCluster(RequestContext context) {
    doChangeRepoRole(context, ClusterInfo.clustered());
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/repo/{corus:role}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void changeRepoRoleForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doChangeRepoRole(context, cluster);
  }
  
  
  @Path({
    "/clusters/{corus:cluster}/repo/pull",
    "/clusters/{corus:cluster}/hosts/repo/pull"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void pullForCluster(RequestContext context) {
    context.getConnector().getRepoFacade().pull(ClusterInfo.clustered());
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/repo/pull")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.DEPLOY)
  public void pullForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getRepoFacade().pull(cluster);
  }

  // --------------------------------------------------------------------------
  
  private void doChangeRepoRole(RequestContext context, ClusterInfo cluster) {
    String roleName = context.getRequest().getValue("corus:role").asString();
    RepoRole role;
    if (roleName.equalsIgnoreCase(CLIENT)) {
      role = RepoRole.CLIENT;
    } else if (roleName.equalsIgnoreCase(SERVER)) {
      role = RepoRole.SERVER;
    } else if (roleName.equalsIgnoreCase(NONE)) {
      role = RepoRole.NONE;
    } else {
      throw new IllegalArgumentException("Unknown repo role: " + roleName);
    }
    context.getConnector().getRepoFacade().changeRole(role, cluster);    
  }
  
  private void streamHost(RequestContext context, JsonStream stream, CorusHost host) {
    stream.beginObject()
      .field("cluster").value(context.getConnector().getContext().getDomain())
      .field("corusVersion").value(context.getConnector().getContext().getVersion())
      .field("hostName").value(host.getHostName())
      .field("hostAddress").value(host.getEndpoint().getServerTcpAddress().getHost())
      .field("port").value(host.getEndpoint().getServerTcpAddress().getPort())
      .field("jvmInfo").value(host.getJavaVmInfo())
      .field("osInfo").value(host.getOsInfo())
      .field("repoRole").value(host.getRepoRole().name());
    stream.endObject();    
  }

}
