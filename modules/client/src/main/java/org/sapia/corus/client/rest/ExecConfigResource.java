package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.ubik.util.Func;

/**
 * Allows reading execution configurations.
 * 
 * @author yduchesne
 *
 */
public class ExecConfigResource {

  @Path({
    "/clusters/{corus:cluster}/exec-configs",
    "/clusters/{corus:cluster}/hosts/exec-configs"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getExecConfigForCluster(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.clustered();
    return doProcessResults(context, context.getConnector().getProcessorFacade().getExecConfigs(cluster));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getExecConfigsForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doProcessResults(context, context.getConnector().getProcessorFacade().getExecConfigs(cluster));
  }

  // --------------------------------------------------------------------------
  // restricted
  
  private String doProcessResults(RequestContext context, Results<List<ExecConfig>> results) {
    final Arg filter = ArgFactory.parse(context.getRequest().getValue("n", "*").asString());
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    
    results = results.filter(new Func<List<ExecConfig>, List<ExecConfig>>() {
      @Override
      public List<ExecConfig> call(List<ExecConfig> toFilter) {
        List<ExecConfig> filtered = new ArrayList<>();
        for (ExecConfig e : toFilter) {
          if (filter.matches(e.getName())) {
            filtered.add(e);
          }
        }
        return filtered;
      }
    });
    
    while (results.hasNext()) {
      Result<List<ExecConfig>> result = results.next();
      for (ExecConfig ec : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("data");
        ec.toJson(stream);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();    
  }
}
