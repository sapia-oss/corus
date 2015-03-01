package org.sapia.corus.http.ping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Date;

import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;

/**
 * Provides a JSON endpoint for pinging this Corus node from a monitoring application.
 * 
 * @author yduchesne
 *
 */
public class PingExtension implements HttpExtension {
  
  public static final String HTTP_PING_CONTEXT = "ping";
  
  private long upSince = System.currentTimeMillis();
  
  @Override
  public HttpExtensionInfo getInfo() {
    return new HttpExtensionInfo()
      .setContextPath(HTTP_PING_CONTEXT)
      .setDescription(String.format("Endpoint used to <a href=\"%s\">ping</a> this Corus node", HTTP_PING_CONTEXT))
      .setName("Ping");
  }
  
  @Override
  public void process(HttpContext ctx) throws Exception, FileNotFoundException {
    StringWriter sw = new StringWriter();
    ctx.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);
    ctx.getResponse().setContentType("application/json");
    JsonStream stream = new WriterJsonStream(sw);
    stream
      .beginObject()
        .field("corusVersion").value(CorusVersion.create().toString())
        .field("upSinceTimestamp").value(upSince)
        .field("upSinceDate").value(new Date(upSince))
        .field("freeMemory").value(Runtime.getRuntime().freeMemory())
        .field("maxMemory").value(Runtime.getRuntime().maxMemory())
        .field("totalMemory").value(Runtime.getRuntime().totalMemory())
        .field("availableProcessors").value(Runtime.getRuntime().availableProcessors())
        .field("fileSystem").beginArray();
    
    for (File root : File.listRoots()) {
      stream.beginObject()
        .field("absolutePath").value(root.getAbsolutePath())
        .field("totalSpace").value(root.getTotalSpace())
        .field("freesSpace").value(root.getFreeSpace())
        .field("usableSpace").value(root.getUsableSpace())
      .endObject();
    }
    stream.endArray().endObject();
    byte[] payload = sw.toString().getBytes();
    ctx.getResponse().setContentLength(payload.length);
    try (OutputStream os = ctx.getResponse().getOutputStream()) {
      os.write(payload);
      os.flush();
    }
  }

}
