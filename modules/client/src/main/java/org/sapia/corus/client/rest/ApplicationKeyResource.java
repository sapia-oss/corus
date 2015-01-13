package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.Permission;

/**
 * Manages application keys.
 * 
 * @author yduchesne
 *
 */
public class ApplicationKeyResource {
  
  // --------------------------------------------------------------------------
  // GET
  
  @Path({
    "/clusters/{corus:cluster}/appkeys",
    "/clusters/{corus:cluster}/hosts/appkeys"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public String getAppKeysForCluster(RequestContext context) {
    return doProcessResults(
        context, 
        context.getConnector().getApplicationKeyManagementFacade().getAppKeyInfos(
            ArgFactory.parse(context.getRequest().getValue("a", "*").asString()), 
            ClusterInfo.clustered()
        )
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/appkeys")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public String getAppKeysForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doProcessResults(
        context, 
        context.getConnector().getApplicationKeyManagementFacade().getAppKeyInfos(
            ArgFactory.parse(context.getRequest().getValue("a", "*").asString()), 
            cluster
        )
    );
  }
  
  // --------------------------------------------------------------------------
  // PUT

  @Path({
    "/clusters/{corus:cluster}/appkeys/{corus:appId}",
    "/clusters/{corus:cluster}/hosts/appkeys/{corus:appId}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void createAppKeyForCluster(RequestContext context) {
    context.getConnector().getApplicationKeyManagementFacade().createApplicationKey(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("k", UUID.randomUUID().toString().replace("-", "").toLowerCase()).asString(),
        context.getRequest().getValue("r").asString(), 
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/appkeys/{corus:appId}")
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void createAppKeyForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getApplicationKeyManagementFacade().createApplicationKey(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("k", UUID.randomUUID().toString().replace("-", "").toLowerCase()).asString(),
        context.getRequest().getValue("r").asString(), 
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // POST
  
  @Path({
    "/clusters/{corus:cluster}/appkeys/{corus:appId}/key/{corus:key}",
    "/clusters/{corus:cluster}/hosts/appkeys/{corus:appId}/key/{corus:key}",
    "/clusters/{corus:cluster}/appkeys/{corus:appId}/key",
    "/clusters/{corus:cluster}/hosts/appkeys/{corus:appId}/key"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void updateAppKeyForCluster(RequestContext context) {
    context.getConnector().getApplicationKeyManagementFacade().changeApplicationKey(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("corus:key", UUID.randomUUID().toString().replace("-", "").toLowerCase()).asString(), 
        ClusterInfo.clustered()
    );
  }
  
  @Path({
      "/clusters/{corus:cluster}/hosts/{corus:host}/appkeys/{corus:appId}/key/{corus:key}",
      "/clusters/{corus:cluster}/hosts/{corus:host}/appkeys/{corus:appId}/key"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void updateAppKeyForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getApplicationKeyManagementFacade().changeApplicationKey(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("corus:key", UUID.randomUUID().toString().replace("-", "").toLowerCase()).asString(), 
        cluster
    );
  }
  
  @Path({
    "/clusters/{corus:cluster}/appkeys/{corus:appId}/role/{corus:role}",
    "/clusters/{corus:cluster}/hosts/appkeys/{corus:appId}/role/{corus:role}"
  })
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void updateAppKeyRoleForCluster(RequestContext context) {
    context.getConnector().getApplicationKeyManagementFacade().changeRole(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("corus:role").asString(), 
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/appkeys/{corus:appId}/role/{corus:role}")
  @HttpMethod(HttpMethod.POST)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void updateAppKeyRoleForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getApplicationKeyManagementFacade().changeRole(
        context.getRequest().getValue("corus:appId").asString(), 
        context.getRequest().getValue("corus:role").asString(), 
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // DELETE

  @Path({
    "/clusters/{corus:cluster}/appkeys/{corus:appId}",
    "/clusters/{corus:cluster}/hosts/appkeys/{corus:appId}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void deleteAppKeyForCluster(RequestContext context) {
    context.getConnector().getApplicationKeyManagementFacade().removeAppKey(
        ArgFactory.parse(context.getRequest().getValue("corus:appId").asString()), 
        ClusterInfo.clustered()
    );
  }
  
  @Path("/clusters/{corus:cluster}/hosts/{corus:host}/appkeys/{corus:appId}")
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.ADMIN)
  public void deleteAppKeyForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    context.getConnector().getApplicationKeyManagementFacade().removeAppKey(
        ArgFactory.parse(context.getRequest().getValue("corus:appId").asString()), 
        cluster
    );
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private String doProcessResults(RequestContext context, Results<List<AppKeyConfig>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<AppKeyConfig>> result = results.next();
      for (AppKeyConfig apk : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("data");
        apk.toJson(stream);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();   
  }
}
