package org.sapia.corus.cloud.platform.rest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.common.base.Predicate;

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
  
  /**
   * @return a {@link JSONArray} containing this instance's {@link JSONObject}s, as they
   * were filtered by the given {@link Condition}.
   */
  public JSONArray asArray(Predicate<JSONObject> filter) {
    JSONArray array = (JSONArray) json;
    JSONArray filtered = new JSONArray();
    for (int i = 0; i < array.size(); i++) {
      JSONObject o = array.getJSONObject(i);
      if (filter.apply(o)) {
        filtered.add(o);
      }
    }
    return filtered;
  }

  @Override
  public String toString() {
    return json.toString();
  }
}
