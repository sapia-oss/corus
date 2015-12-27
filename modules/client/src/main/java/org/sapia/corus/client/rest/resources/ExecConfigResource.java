package org.sapia.corus.client.rest.resources;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
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
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/exec-configs"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getExecConfigForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    String name = context.getRequest().getValue("n", "*").asString();
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(name)).build();
    return doProcessResults(context, context.getConnector().getProcessorFacade().getExecConfigs(crit, targets));
  }

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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(name)).build();
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
    ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgMatchers.parse(name)).build();
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
          .field("dataType").value("exec-config")
          .field("data");
        ec.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();    
  }
}
