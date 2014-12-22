package org.sapia.corus.client.rest;

import java.io.StringWriter;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * A REST resources that allows viewing cluster-related information.
 * 
 * @author yduchesne
 *
 */
public class ClusterResource {

  @Path("/clusters")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getClusters(RequestContext context) {
    StringWriter     output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream
      .beginArray()
        .beginObject()
          .field("name").value(context.getConnector().getContext().getDomain())
        .endObject()
      .endArray();
    return output.toString();   
  }
  
  @Path("/clusters/{corus:cluster}/hosts")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getHostsForCluster(RequestContext context) {
    StringWriter     output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    streamHost(context, stream, context.getConnector().getContext().getServerHost());
    for (CorusHost host : context.getConnector().getContext().getOtherHosts()) {
      streamHost(context, stream, host);
    }
    stream.endArray();
    return output.toString();   
  }
  
  // --------------------------------------------------------------------------

  private void streamHost(RequestContext context, JsonStream stream, CorusHost host) {
    stream.beginObject()
      .field("cluster").value(context.getConnector().getContext().getDomain())
      .field("corusVersion").value(context.getConnector().getContext().getVersion())
      .field("hostName").value(host.getHostName())
      .field("hostAddress").value(host.getEndpoint().getServerTcpAddress().getHost())
      .field("port").value(host.getEndpoint().getServerTcpAddress().getPort())
      .field("jvmInfo").value(host.getJavaVmInfo())
      .field("osInfo").value(host.getOsInfo())
      .field("repoRole").value(host.getRepoRole().name());
    stream.endObject();    
  }

}
