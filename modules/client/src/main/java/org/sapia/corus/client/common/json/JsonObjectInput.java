package org.sapia.corus.client.common.json;

import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Implements the {@link JsonInput} interface on top of the {@link JSONObject} class.
 * 
 * @author yduchesne
 *
 */
public class JsonObjectInput implements JsonInput {
  
  private JSONObject delegate;
  
  public JsonObjectInput(String json) {
    this(JSONObject.fromObject(json));
  }
  
  public JsonObjectInput(JSONObject delegate) {
    this.delegate = delegate;
  }

  // --------------------------------------------------------------------------
  // Factory methods
  
  /**
   * @param delegate a {@link JSONObject} to wrap.
   * @return a new {@link JsonObjectInput}, wrapping the given {@link JSONObject}.
   */
  public static JsonObjectInput newInstance(JSONObject delegate) {
    return new JsonObjectInput(delegate);
  }
  
  /**
   * @param jsonContent a JSON content string.
   * @return a new {@link JsonObjectInput}, corresponding to the given JSON content.
   */
  public static JsonObjectInput newInstance(String jsonContent) {
    return new JsonObjectInput(jsonContent);
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public boolean getBoolean(String field) {
    return delegate.getBoolean(field);
  }
  
  @Override
  public int getInt(String field) {
    return delegate.getInt(field);
  }
  
  @Override
  public long getLong(String field) {
    return delegate.getLong(field);
  }
  
  @Override
  public String getString(String field) {
    return delegate.getString(field);
  }
  
  @Override
  public String[] getStringArray(String field) {
    JSONArray arr = delegate.getJSONArray(field);
    String[] toReturn = new String[arr.size()];
    for (int i = 0; i < arr.size(); i++) {
      toReturn[i] = arr.getString(i);
    }
    return toReturn;
  }
  
  @Override
  public int[] getIntArray(String field) {
    JSONArray arr = delegate.getJSONArray(field);
    int[] toReturn = new int[arr.size()];
    for (int i = 0; i < arr.size(); i++) {
      toReturn[i] = arr.getInt(i);
    }
    return toReturn;
  }
  
  @Override
  public Integer[] getIntObjectArray(String field) {
    JSONArray arr = delegate.getJSONArray(field);
    Integer[] toReturn = new Integer[arr.size()];
    for (int i = 0; i < arr.size(); i++) {
      toReturn[i] = arr.getInt(i);
    }
    return toReturn;
  }
  
  @Override
  public JsonInput getObject(String field) {
    return new JsonObjectInput(delegate.getJSONObject(field));
  }
  
  @Override
  public Iterable<JsonInput> iterate(String field) {
    final JSONArray array = delegate.getJSONArray(field);
    return new Iterable<JsonInput>() {
      @Override
      public Iterator<JsonInput> iterator() {
        return new Iterator<JsonInput>() {
          int index = 0;
          @Override
          public JsonInput next() {
            return new JsonObjectInput(array.getJSONObject(index++));
          }
          public boolean hasNext() {
            return index < array.size();
          }
        };
      }
    };
  }

}
