package org.sapia.corus.client.rest.resources;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticStatus;

/**
 * A REST resource providing diagnostic data.
 * 
 * @author yduchesne
 *
 */
public class DiagnosticResource { 
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/diagnostic"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getDiagnosticsForPartition(RequestContext context, RestResponseFacade responseFacade) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doGetDiagnostics(context, targets, responseFacade);
  }
  
  @Path({
    "/clusters/{corus:cluster}/diagnostic",
    "/clusters/{corus:cluster}/hosts/diagnostic"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getDiagnosticsForCluster(RequestContext context, RestResponseFacade responseFacade) {
    return doGetDiagnostics(context, ClusterInfo.clustered(), responseFacade);
  }
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/diagnostic"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getDiagnosticsForHost(RequestContext context, RestResponseFacade responseFacade) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetDiagnostics(context, cluster, responseFacade);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetDiagnostics(RequestContext context, ClusterInfo cluster, RestResponseFacade responseFacade) {
    
    Results<GlobalDiagnosticResult> results = context.getConnector()
        .getDiagnosticFacade()
        .acquireDiagnostics(cluster);
    
    Value        contentLevelName = context.getRequest().getValue("contentLevel", "minimal");
    ContentLevel contentLevel     = ContentLevel.forName(contentLevelName.asString());
    
    StringWriter     output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    List<GlobalDiagnosticResult> flattenedResults = new ArrayList<GlobalDiagnosticResult>();
    while (results.hasNext()) {
      Result<GlobalDiagnosticResult> result = results.next();
      stream.beginObject()
        .field("cluster").value(context.getConnector().getContext().getDomain())
        .field("host").value(
            result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
            result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
        )
        .field("data");
      result.getData().toJson(stream, contentLevel);
      stream.endObject();
      flattenedResults.add(result.getData());
    }
    stream.endArray();
    
    int errorCount   = 0;
    int pendingCount = 0;
    for (GlobalDiagnosticResult r : flattenedResults) {
      if (r.getStatus() == GlobalDiagnosticStatus.FAILURE) {
        errorCount++;
        break;
      } else if (r.getStatus() == GlobalDiagnosticStatus.INCOMPLETE) {
        pendingCount++;
      }
    }

    if (errorCount > 0) {
      responseFacade.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      responseFacade.setStatusMessage("Diagnostic check revealed an issue, check response data for details.");
    } else if (pendingCount > 0) {
      responseFacade.setStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
      responseFacade.setStatusMessage("Diagnostic check cannot be completed at this time (system not ready or busy). Retry in 10 seconds.");
    }
    return output.toString();    
  }
}
