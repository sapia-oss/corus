package org.sapia.corus.client.common.json;

import java.io.StringWriter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Internally outputs to a {@link StringWriter}. Calling {@link #toString()} on
 * an instance of this class returns the JSON string resulting from the output.
 * 
 * @author yduchesne
 *
 */
public class StringWriterJsonStream extends WriterJsonStream {
  
  public StringWriterJsonStream() {
    super(new StringWriter());
  }

  /**
   * Returns the JSON generated thus far.
   */
  @Override
  public String toString() {
    return delegateWriter().toString();
  }
  
  /**
   * @return this instance's JSON content, as a {@link JSONObject}.
   */
  public JSONObject toJsonObject() {
    return JSONObject.fromObject(toString());
  }
  
  /**
   * @return this instance's JSON content, as a {@link JSONArray}.
   */
  public JSONArray toJsonArray() {
    return JSONArray.fromObject(toString());
  }

}
