package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.ubik.util.Func;

/**
 * A REST resources that gives access to {@link PortRange}s.
 * 
 * @author yduchesne
 *
 */
public class PortResource {

  @Path({
    "/clusters/{corus:cluster}/ports/ranges",
    "/clusters/{corus:cluster}/hosts/ports/ranges"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getPortRangesForCluster(RequestContext context) {
    return doGetPortRanges(context, ClusterInfo.clustered());
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/ports/ranges"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getPortRangesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetPortRanges(context, cluster);
  }
    
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetPortRanges(RequestContext context, ClusterInfo cluster) {
    final ArgMatcher filter = ArgMatchers.parse(context.getRequest().getValue("n", "*").asString());
    Results<List<PortRange>> results = context.getConnector()
        .getPortManagementFacade()
        .getPortRanges(cluster).filter(new Func<List<PortRange>, List<PortRange>>() {
          
          @Override
          public List<PortRange> call(List<PortRange> ranges) {
            List<PortRange> filtered = new ArrayList<>();
            for (PortRange r : ranges) {
              if (filter.matches(r.getName())) {
                filtered.add(r);
              }
            }
            return filtered;
          }
        });
    
    return doProcessPortRangeResults(context, results);
  }

  private String doProcessPortRangeResults(RequestContext context, Results<List<PortRange>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<PortRange>> result = results.next();
      for (PortRange r : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("data");
        r.toJson(stream);
        stream.endObject();
      }
    }
    stream.endArray();
    return output.toString();    
  }

}
