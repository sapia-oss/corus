package org.sapia.corus.client.rest;

import java.io.StringWriter;

import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;

/**
 * Provides metadata about the system. 
 * 
 * @author yduchesne
 *
 */
public class MetadataResource {
  
  @Path("/metadata/status")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getStatusMetadata(RequestContext context) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    for (LifeCycleStatus st : LifeCycleStatus.values()) {
      stream.beginObject()
        .field("name").value(st.name())
        .field("description").value(st.description())
      .endObject();
    }
    stream.endArray();
    return output.toString();
  }

}
