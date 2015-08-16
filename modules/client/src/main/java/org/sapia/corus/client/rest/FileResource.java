package org.sapia.corus.client.rest;

import java.io.StringWriter;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;

/**
 * A REST resources that gives access to files.
 * 
 * @author yduchesne
 *
 */
public class FileResource {

  @Path({
    "/clusters/{corus:cluster}/partitionsets/{corus:partitionSetId}/partitions/{corus:partitionIndex}/files"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getFilesForPartition(RequestContext context) {
    ClusterInfo targets = context.getPartitionService()
        .getPartitionSet(context.getRequest().getValue("corus:partitionSetId").asString())
        .getPartition(context.getRequest().getValue("corus:partitionIndex").asInt())
        .getTargets();
    return doGetFiles(context, targets);
  }  
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/files",
    "/clusters/{corus:cluster}/hosts/files",
    "/clusters/{corus:cluster}/files/{corus:name}",
    "/clusters/{corus:cluster}/hosts/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getFilesForCluster(RequestContext context) {
    return doGetFiles(context, ClusterInfo.clustered());
  }  
  
  // --------------------------------------------------------------------------
  
  @Path({
    "/clusters/{corus:cluster}/hosts/{corus:host}/files",
    "/clusters/{corus:cluster}/hosts/{corus:host}/files/{corus:name}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getFilesForHost(RequestContext context) {
    ClusterInfo cluster = ClusterInfo.fromLiteralForm(context.getRequest().getValue("corus:host").asString());
    return doGetFiles(context, cluster);
  }
    
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private String doGetFiles(RequestContext context, ClusterInfo cluster) {
    FileCriteria criteria = FileCriteria.newInstance();
    criteria.setName(ArgMatchers.parse(context.getRequest().getValue("corus:name", "*").asString()));

    Results<List<FileInfo>> results = context.getConnector()
        .getFileManagementFacade()
        .getFiles(criteria, cluster);
    
    return doProcessResults(context, results);
  }
  
  private String doProcessResults(RequestContext context, Results<List<FileInfo>> results) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    while (results.hasNext()) {
      Result<List<FileInfo>> result = results.next();
      for (FileInfo f : result.getData()) {
        stream.beginObject()
          .field("cluster").value(context.getConnector().getContext().getDomain())
          .field("host").value(
              result.getOrigin().getEndpoint().getServerTcpAddress().getHost() + ":" +
              result.getOrigin().getEndpoint().getServerTcpAddress().getPort()
          )
          .field("dataType").value("file")
          .field("data");
        f.toJson(stream, ContentLevel.DETAIL);
        stream.endObject();
      }      
    }
    stream.endArray();
    return output.toString();    
  }
}
