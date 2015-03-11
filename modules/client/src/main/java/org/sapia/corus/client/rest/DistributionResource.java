package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * A REST resources that gives access to {@link Distribution}s.
 * 
 * @author yduchesne
 *
 */
public class DistributionResource {

  @Path({
    "/clusters/{corus:cluster}/distributions",
    "/clusters/{corus:cluster}/hosts/distributions"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getDistributionsForCluster(RequestContext context) {
    return doGetDistributions(context, ClusterInfo.clustered());
  }  
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/distributions"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getDistributionsForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetDistributions(context, cluster);
  }
    
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetDistributions(RequestContext context, ClusterInfo cluster) {
    DistributionCriteria.Builder criteria = DistributionCriteria.builder();
    criteria.name(ArgMatchers.parse(context.getRequest().getValue("d", "*").asString()));
    criteria.version(ArgMatchers.parse(context.getRequest().getValue("v", "*").asString()));
    
    Results<List<Distribution>> results = context.getConnector()
        .getDeployerFacade()
        .getDistributions(criteria.build(), cluster);
    
    return doProcessResults(context, results);
  }
  
  private String doProcessResults(RequestContext context, Results<List<Distribution>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<Distribution>> result = results.next();
      for (Distribution d : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("data");
        d.toJson(stream);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();    
  }
}
