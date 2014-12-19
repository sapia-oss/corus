package org.sapia.corus.client.common.json;

import java.util.Date;
import java.util.List;

/**
 * A callback interface hiding the implementation details of JSON generation.
 * 
 * @author yduchesne
 *
 */
public interface JsonStream {

  /**
   * Signals the beginning of a JSON object.
   */
  public JsonStream beginObject();
 
  /**
   * Signals the beginning of a JSON array.
   */
  public JsonStream beginArray();
  
  /**
   * @param name a field name.
   */
  public JsonStream field(String name);
  
  /**
   * @param value a {@link String} value corresponding to the specified field.
   */
  public JsonStream value(String value);
  
  /**
   * @param value a {@link Number} value corresponding to the specified field.
   */
  public JsonStream value(Number value);
  
  /**
   * @param value a {@link Date} value corresponding to the specified field.
   */
  public JsonStream value(Date value);

  /**
   * @param value a <code>boolean</code> value corresponding to the specified field.
   */
  public JsonStream value(boolean value);
  
  /**
   * @param values an array of {@link String} values.
   */
  public JsonStream strings(String[] values);
  
  /**
   * @param values a {@link List}  of {@link String} values.
   */
  public JsonStream strings(List<String> values);
  
  /**
   * @param values an array of {@link Number} values.
   */
  public JsonStream numbers(Number[] values);
  
  /**
   * @param values a {@link List} of {@link Number} values.
   */
  public JsonStream numbers(List<? extends Number> values);
  
  /**
   * Signals the end of a JSON array.
   */
  public JsonStream endArray();
  
  /**
   * Signals the end of a JSON object.
   */
  public JsonStream endObject();
  
}
