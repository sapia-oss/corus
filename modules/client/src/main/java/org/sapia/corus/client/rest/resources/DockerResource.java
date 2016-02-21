package org.sapia.corus.client.rest.resources;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.docker.DockerImage;

/**
 * Returns Docker objects.
 * 
 * @author yduchesne
 *
 */
public class DockerResource {

  // ==========================================================================
  // get images

  // --------------------------------------------------------------------------
  // partitions
  
  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/docker/images"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getImagesForPartition(RequestContext context) throws Exception {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doGetImages(context, targets);
  }  
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Path({
    "/clusters/{corus:cluster}/docker/images",
    "/clusters/{corus:cluster}/hosts/docker/images"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getImagesForCluster(RequestContext context) throws Exception {
    return doGetImages(context, ClusterInfo.clustered());
  }  
  
  // --------------------------------------------------------------------------
  // host
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/docker/images"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getImagesForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetImages(context, cluster);
  }
  
  // ==========================================================================
  // get image payload
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/docker/images/{corus:image}/payload"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_OCTET_STREAM)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public InputStream getImagePayloadForHost(RequestContext context) throws Exception {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    CorusHost currentHost  = context.getConnector().getContext().getServerHost();
    CorusHost selectedHost = context.getConnector().getContext().resolve(cluster.getTargets().iterator().next());
    String imageName = context.getRequest().getValue("corus:image").notNull().asString();
    if (currentHost.equals(selectedHost)) {
      return doGetImagePayloadForHost(context, imageName);
    } else {
      try {
        context.getConnector().getContext().connect(selectedHost);
        return doGetImagePayloadForHost(context, imageName);
      } finally {
        context.getConnector().getContext().connect(currentHost);
      }
    }
  } 
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetImages(RequestContext context, ClusterInfo cluster) throws Exception {
    ArgMatcher criterion = ArgMatchers.parse(context.getRequest().getValue("n", "*").asString());
    
    Results<List<DockerImage>> results = context.getConnector()
        .getDockerManagementFacade()
        .getImages(criterion, cluster);
    
    return doProcessResults(context, results);
  }
 
  private InputStream doGetImagePayloadForHost(RequestContext context, String imageName) throws Exception {
    return context.getConnector().getDockerManagementFacade().getImagePayload(imageName);
  }
  
  private String doProcessResults(RequestContext context, Results<List<DockerImage>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<DockerImage>> result = results.next();
      for (DockerImage d : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("dataType").value("dockerImage")
          .field("data");
        d.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();    
  }
}
