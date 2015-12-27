package org.sapia.corus.client.rest.resources;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.ResourceNotFoundException;
import org.sapia.corus.client.services.processor.PortCriteria;
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
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/processes"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getProcessesForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doGetProcesses(context, targets);
  }
  
  // --------------------------------------------------------------------------
  
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
    criteria.pid(ArgMatchers.parse(context.getRequest().getValue("corus:process_id").asString()));
    Results<List<org.sapia.corus.client.services.processor.Process>> results = context.getConnector()
        .getProcessorFacade()
        .getProcesses(criteria.build(), cluster);
    if (results.hasNext()) {
      Result<List<Process>> r = results.next();
      if (r.getData().isEmpty()) {
        throw new ResourceNotFoundException("No process for: " + context.getRequest().getValue("corus:process_id"));        
      } else if (r.getData().size() != 1) {
        throw new IllegalStateException("More than one process for " + context.getRequest().getValue("corus:process_id"));
      } else {
        StringWriter output = new StringWriter();
        Process p = r.getData().get(0);
        WriterJsonStream stream = new WriterJsonStream(output);
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              r.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              r.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("dataType").value("process")
          .field("data");  
        p.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();  
        return output.toString();    
      }
    } else {
      throw new ResourceNotFoundException("No process for: " + context.getRequest().getValue("corus:process_id"));
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetProcesses(RequestContext context, ClusterInfo cluster) {
    ProcessCriteria.Builder criteria = ProcessCriteria.builder();
    criteria.distribution(ArgMatchers.parse(context.getRequest().getValue("d", "*").asString()));
    criteria.version(ArgMatchers.parse(context.getRequest().getValue("v", "*").asString()));
    criteria.name(ArgMatchers.parse(context.getRequest().getValue("n", "*").asString()));
    
    Value portRangePattern = context.getRequest().getValue("pr");
    if (portRangePattern.isSet()) {
      criteria.ports(PortCriteria.fromLiteral(portRangePattern.asString()));
    }
    
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
          .field("dataType").value("process")
          .field("data");  
        p.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();  
      }
    }
    stream.endArray();
    return output.toString();    
  }
}
