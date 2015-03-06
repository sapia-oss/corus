package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;

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
    String name = context.getRequest().getValue("n", "*").asString();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(name)).build();
    return doProcessResults(context, context.getConnector().getProcessorFacade().getExecConfigs(crit, cluster));
  }
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/exec-configs"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getExecConfigsForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    String name = context.getRequest().getValue("n", "*").asString();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(name)).build();
    return doProcessResults(context, context.getConnector().getProcessorFacade().getExecConfigs(crit, cluster));
  }

  // --------------------------------------------------------------------------
  // restricted
  
  private String doProcessResults(RequestContext context, Results<List<ExecConfig>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    
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
