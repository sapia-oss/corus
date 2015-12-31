package org.sapia.corus.client.rest.resources;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;

/**
 * Manages roles.
 * 
 * @author yduchesne
 *
 */
public class RoleResource {
  
  // --------------------------------------------------------------------------
  // GET

  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/roles"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public String getRolesForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doProcessResults(
        context, 
        context.getConnector().getSecurityManagementFacade().getRoleConfig(
            ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()), 
            targets
        )
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/roles", 
    "/clusters/{corus:cluster}/hosts/roles"}
  )
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public String getRolesForCluster(RequestContext context) {
    return doProcessResults(
        context, 
        context.getConnector().getSecurityManagementFacade().getRoleConfig(
            ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()), 
            ClusterInfo.clustered()
        )
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/roles")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public String getRolesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doProcessResults(
        context, 
        context.getConnector().getSecurityManagementFacade().getRoleConfig(
            ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()), 
            cluster
        )
    );
  }
  
  // --------------------------------------------------------------------------
  // PUT

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/roles/{corus:role}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void createOrUpdateRoleForParitition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    context.getConnector().getSecurityManagementFacade().addOrUpdateRole(
        context.getRequest().getValue("corus:role").asString(), 
        Permission.forPermissionSet(context.getRequest().getValue("permissions").asString()),
        targets
    );
  }
  
  @Path({
      "/clusters/{corus:cluster}/roles/{corus:role}", 
      "/clusters/{corus:cluster}/hosts/roles/{corus:role}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void createOrUpdateRoleForCluster(RequestContext context) {
    context.getConnector().getSecurityManagementFacade().addOrUpdateRole(
        context.getRequest().getValue("corus:role").asString(), 
        Permission.forPermissionSet(context.getRequest().getValue("permissions").asString()),
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/roles/{corus:role}")
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void createOrUpdateRoleForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getSecurityManagementFacade().addOrUpdateRole(
        context.getRequest().getValue("corus:role").asString(), 
        Permission.forPermissionSet(context.getRequest().getValue("permissions").asString()),
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // DELETE

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/roles/{corus:role}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void deleteRoleForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    context.getConnector().getSecurityManagementFacade().removeRole(
        context.getRequest().getValue("corus:role").asString(), 
        targets
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/roles/{corus:role}", 
    "/clusters/{corus:cluster}/hosts/roles/{corus:role}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void deleteRoleForCluster(RequestContext context) {
    context.getConnector().getSecurityManagementFacade().removeRole(
        context.getRequest().getValue("corus:role").asString(), 
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/roles/{corus:role}")
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void deleteRoleForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getSecurityManagementFacade().removeRole(
        context.getRequest().getValue("corus:role").asString(), 
        cluster
    );
  }
  
  private String doProcessResults(RequestContext context, Results<List<RoleConfig>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<RoleConfig>> result = results.next();
      for (RoleConfig r : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("dataType").value("role")
          .field("data");
        r.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();   
  }
}
