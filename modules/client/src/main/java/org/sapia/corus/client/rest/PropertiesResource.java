package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * A REST resources that gives access to process and server properties.
 * 
 * @author yduchesne
 *
 */
public class PropertiesResource {
  
  private static final String SCOPE_SERVER    = "server";
  private static final String SCOPE_PROCESS   = "process";
  
  @Path({
    "/clusters/{corus:cluster}/properties/{corus:scope}",
    "/clusters/{corus:cluster}/properties/{corus:scope}/{corus:category}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}",
    "/clusters/{corus:cluster}/hosts/properties/{corus:scope}/{corus:category}"    
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getPropertiesForCluster(RequestContext context) {
    return doGetProperties(context, ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}",
    "/clusters/{corus:cluster}/hosts/{corus:host}/properties/{corus:scope}/{corus:category}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getPropertiesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetProperties(context, cluster);
  }
    
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetProperties(RequestContext context, ClusterInfo cluster) {
    String        scopeValue = context.getRequest().getValue("corus:scope").asString();
    final Value   category   = context.getRequest().getValue("corus:category");
    PropertyScope scope;
    if (scopeValue.equals(SCOPE_PROCESS)) {
      scope = PropertyScope.PROCESS;
    } else if (scopeValue.equals(SCOPE_SERVER)) {
      scope = PropertyScope.SERVER;
    } else {
      throw new IllegalArgumentException(String.format("Invalid scope %s. Use one of the supported scopes: [process, server]", scopeValue));
    }
    
    final ArgMatcher propNameFilter = ArgMatchers.parse(context.getRequest().getValue("p", "*").asString());
    final ArgMatcher catFilter      = category.isNull() ? null : ArgMatchers.parse(category.asString()); 
    Set<ArgMatcher>  catFilters     = catFilter == null ? new HashSet<ArgMatcher>() : Collects.arrayToSet(catFilter);
    Results<List<Property>> results = context.getConnector()
        .getConfigFacade().getAllProperties(scope, catFilters, cluster);
    results = results.filter(new Func<List<Property>, List<Property>>() {
      @Override
      public List<Property> call(List<Property> toFilter) {
        List<Property> toReturn = new ArrayList<>();
        for (Property p : toFilter) {
          if (propNameFilter.matches(p.getName())) {
            toReturn.add(p);
          }
        }
        return toReturn;
      }
    });
    return doProcessResults(context, results);
  }
  
  private String doProcessResults(RequestContext context, Results<List<Property>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<Property>> result = results.next();
      stream.beginObject()
        .field("cluster").value(context.getConnector().getContext().getDomain())
        .field("host").value(
            result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
            result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
        )
        .field("data")
        .beginArray();

      for (Property np : result.getData()) {
        stream.beginObject()
          .field("name").value(np.getName())
          .field("value").value(obfuscate(np.getName(), np.getValue(), context));
        if (np.getCategory().isSet()) {
          stream.field("category").value(np.getCategory().get());
        }  
        stream.endObject();
      }
      stream.endArray().endObject();
    }
    stream.endArray();
    return output.toString();    
  }
  
  private String obfuscate(String propertyName, String propertyValue, RequestContext context) {
    ConfiguratorFacade cfg = context.getConnector().getConfigFacade();
    return cfg.getPropertyMasker().getMaskedValue(propertyName, propertyValue);
  }
} 
