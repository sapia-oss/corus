package org.sapia.corus.client.rest;

import java.io.StringWriter;

import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.security.Permission;

/**
 * Provides metadata about the system. 
 * 
 * @author yduchesne
 *
 */
public class MetadataResource {
  
  @Path("/metadata/status/lifecycle")
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

  @Path("/metadata/security/permissions")
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  public String getPermissionsMetadata(RequestContext context) {
    StringWriter output = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(output);
    stream.beginArray();
    for (Permission p : Permission.values()) {
      stream.beginObject()
        .field("name").value(p.name())
        .field("abbreviation").value("" + p.abbreviation())
      .endObject();
    }
    stream.endArray();
    return output.toString();
  }
}
