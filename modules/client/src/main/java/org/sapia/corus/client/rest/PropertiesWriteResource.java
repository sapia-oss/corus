package org.sapia.corus.client.rest;

import java.util.HashSet;
import java.util.Properties;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.annotations.Authorized;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.util.Collects;

/**
 * Handles the addition/deletion of properties.
 * 
 * @author yduchesne
 *
 */
public class PropertiesWriteResource {

  private static final String SCOPE_SERVER  = "server";
  private static final String SCOPE_PROCESS = "process";
  
  @Path({
    "/clusters/{corus:cluster}/properties/{corus:scope}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}",
    "/clusters/{corus:cluster}/properties/{corus:scope}/{corus:category}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}/{corus:category}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addPropertiesForCluster(RequestContext context) {
    doAddProperties(context, ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}",
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}/{corus:category}"
  })
  @HttpMethod(HttpMethod.PUT)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void addPropertiesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doAddProperties(context, cluster);
  }
  
  // --------------------------------------------------------------------------

  @Path({
    "/clusters/{corus:cluster}/properties/{corus:scope}/{corus:propertyName}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}/{corus:propertyName}",
    "/clusters/{corus:cluster}/properties/{corus:scope}/{corus:propertyName}/{corus:category}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}/{corus:propertyName}/{corus:category}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deletePropertyForCluster(RequestContext context) {
    doDeleteProperty(context, ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}/{corus:propertyName}",
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}/{corus:propertyName}/{corus:category}"
  })
  @HttpMethod(HttpMethod.DELETE)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @Authorized(Permission.WRITE)
  public void deletePropertyForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    doDeleteProperty(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private void doAddProperties(RequestContext context, ClusterInfo cluster) {
    Properties props = new Properties();
    for (Value v : context.getRequest().getValues()) {
      if (!v.getName().equals(HttpExtension.CORUS_PARAM_APP_ID) 
          && !v.getName().equals(HttpExtension.CORUS_PARAM_APP_KEY)) {
        props.setProperty(v.getName(), v.asString());
      }
    }    
    Value category = context.getRequest().getValue("corus:category");
    if (category.isNull()) {
      context.getConnector().getConfigFacade().addProperties(
          getScope(context), 
          props, 
          new HashSet<String>(0),
          context.getRequest().getValue("clearExisting", "false").asBoolean(), cluster);
    } else {
      context.getConnector().getConfigFacade().addProperties(
          getScope(context), 
          props, 
          category.asSet(),
          context.getRequest().getValue("clearExisting", "false").asBoolean(), cluster);
    }
  }
  
  private void doDeleteProperty(RequestContext context, ClusterInfo cluster) {
    Value category = context.getRequest().getValue("corus:category");
    if (category.isNull()) {
      context.getConnector().getConfigFacade().removeProperty(
          getScope(context), 
          ArgFactory.parse(context.getRequest().getValue("corus:propertyName").asString()), 
          new HashSet<Arg>(0),
          cluster
      );
    } else {
      context.getConnector().getConfigFacade().removeProperty(
          getScope(context), 
          ArgFactory.parse(context.getRequest().getValue("corus:propertyName").asString()), 
          Collects.arrayToSet(ArgFactory.parse(category.asString())),
          cluster
      );      
    }
  }
  
  private PropertyScope getScope(RequestContext context) {
    String scopeValue = context.getRequest().getValue("corus:scope").asString();
    PropertyScope scope;
    if (scopeValue.equals(SCOPE_PROCESS)) {
      scope = PropertyScope.PROCESS;
    } else if (scopeValue.equals(SCOPE_SERVER)) {
      scope = PropertyScope.SERVER;
    } else {
      throw new IllegalArgumentException(String.format("Invalid scope %s. Use one of the supported scopes: [process, server]", scopeValue));
    }
    return scope;
  }
}
