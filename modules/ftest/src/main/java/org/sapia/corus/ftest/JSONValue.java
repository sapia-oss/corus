package org.sapia.corus.ftest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Encapsulates either a {@link JSONArray} or {@link JSONObject}, depending on the
 * constructor that's called.
 * 
 * @author yduchesne
 * 
 */
public class JSONValue {
  
  private Object json;
  
  public JSONValue(JSONObject json) {
    this.json = json;
  }
  
  public JSONValue(JSONArray json) {
    this.json = json;
  }
  
  /**
   * @return <code>true</code> if this instance holds a {@link JSONArray}, <code>false</code>
   * if it holds a {@link JSONObject}.
   */
  public boolean isArray() {
    return json instanceof JSONArray;
  }
  
  /**
   * @return this instance's content as a {@link JSONObject}.
   */
  public JSONObject asObject() {
    return (JSONObject) json;
  }
  
  /**
   * @return this instance's content as a {@link JSONArray}.
   */
  public JSONArray asArray() {
    return (JSONArray) json;
  }

  @Override
  public String toString() {
    return json.toString();
  }
}
