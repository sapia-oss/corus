package org.sapia.corus.cloud.platform.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * A custom JSON converter that's plugged into the Jersey framework.
 * 
 * @author yduchesne
 *  
 */
public class JsonMessageBodyConverter implements MessageBodyWriter<JSONObject>, MessageBodyReader<JSONValue> {

  @Override
  public long getSize(JSONObject arg0, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4) {
    return -1;
  }
  
  @Override
  public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return true;
  }
  
  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return true;
  }
  
  @Override
  public JSONValue readFrom(Class<JSONValue> arg0, Type arg1,
      Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
      InputStream input) throws IOException, WebApplicationException {
    String json = new String(IOUtils.toByteArray(input), "UTF-8").trim();
    input.close();
    if (json.startsWith("[")) {
      return new JSONValue(JSONArray.fromObject(json));
    } else {
      return new JSONValue(JSONObject.fromObject(json));
    }
  }
  
  @Override
  public void writeTo(JSONObject json, Class<?> arg1, Type arg2,
      Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5,
      OutputStream out) throws IOException, WebApplicationException {
    PrintWriter pw = new PrintWriter(out);
    pw.print(json.toString());
    pw.flush();
    
  }
}
