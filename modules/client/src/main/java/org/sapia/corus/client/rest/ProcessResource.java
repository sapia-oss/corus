package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * A REST resources that gives access to {@link Process}es.
 * 
 * @author yduchesne
 *
 */
public class ProcessResource {

  @Path({
    "/clusters/{corus:cluster}/processes",
    "/clusters/{corus:cluster}/hosts/processes"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getProcessesForCluster(RequestContext context) {
    return doGetProcesses(context, ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getProcessesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetProcesses(context, cluster);
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/processes/{corus:process_id}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getProcess(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.pid(ArgFactory.parse(context.getRequest().getValue("corus:process_id").asString()));
    Results<List<org.sapia.corus.client.services.processor.Process>> results = context.getConnector()
        .getProcessorFacade()
        .getProcesses(criteria.build(), cluster);
    return doProcessResults(context, results);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetProcesses(RequestContext context, ClusterInfo cluster) {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgFactory.parse(context.getRequest().getValue("d", "*").asString()));
    criteria.version(ArgFactory.parse(context.getRequest().getValue("v", "*").asString()));
    criteria.name(ArgFactory.parse(context.getRequest().getValue("n", "*").asString()));
    Value profile = context.getRequest().getValue("p");
    if (profile.isSet()) {
      criteria.profile(profile.asString());
    }    
    
    Results<List<org.sapia.corus.client.services.processor.Process>> results = context.getConnector()
        .getProcessorFacade()
        .getProcesses(criteria.build(), cluster);
    
    return doProcessResults(context, results);
  }
  
  private String doProcessResults(RequestContext context, Results<List<org.sapia.corus.client.services.processor.Process>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<Process>> result = results.next();
      for (Process p : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("data");  
        p.toJson(stream);
        stream.endObject();  
      }
    }
    stream.endArray();
    return output.toString();    
  }
}
