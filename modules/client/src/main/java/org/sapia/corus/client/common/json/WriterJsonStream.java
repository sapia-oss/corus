package org.sapia.corus.client.common.json;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.TimeZone;

/**
 * Implements the {@link JsonStream} interface over the {@link Writer} class.
 * 
 * @author yduchesne
 *
 */
public class WriterJsonStream implements JsonStream {
  
  private static class ObjectState {
    
    private int     elementCount;
    private boolean isArray;
    private int     fieldCount;
    
  }
  
  static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss:sss'Z'");
  static {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DATE_FORMAT.setTimeZone(tz);
  }
  
  private PrintWriter     writer;
  private Stack<ObjectState> states = new Stack<ObjectState>();
  
  /**
   * @param writer the {@link Writer} to write to.
   */
  public WriterJsonStream(Writer writer) {
    this.writer = new PrintWriter(writer, true);
  }

  @Override
  public JsonStream beginObject() {
    if (!states.isEmpty() && states.peek().isArray) {
      ObjectState state = states.peek();
      if (state.elementCount > 0) {
        writer.print(",");        
      }
      state.elementCount++;
    }
    writer.print("{");
    states.push(new ObjectState());
    return this;
  }

  @Override
  public JsonStream beginArray() {
    ObjectState state = new ObjectState();
    state.isArray = true;
    states.push(state);
    writer.print("[");
    return this;
  }

  @Override
  public JsonStream field(String name) {
    if (!states.isEmpty()) {
      ObjectState state = states.peek();
      if (state.fieldCount > 0) {
        writer.print(",");
      }
      state.fieldCount++;
    }
    writer.print("\"" + name + "\":");
    return this;
  }

  @Override
  public JsonStream value(String value) {
    writer.print("\"" + value + "\"");
    return this;
  }

  @Override
  public JsonStream value(Number value) {
    writer.print(value);
    return this;
  }

  @Override
  public JsonStream value(Date value) {
    synchronized (DATE_FORMAT) {
      writer.print("\"" + DATE_FORMAT.format(value) + "\"");     
    }
    return this;
  }

  @Override
  public JsonStream value(boolean value) {
    writer.print(value ? "true" : "false");
    return this;
  }

  @Override
  public JsonStream strings(String[] values) {
    return strings(Arrays.asList(values));
  }

  @Override
  public JsonStream strings(List<String> values) {
    writer.print("[");
    int valueCount = 0;
    for (String v : values) {
      if (valueCount > 0) {
        writer.print(",");
      }
      writer.print("\"" + v + "\"");
      valueCount++;
    }
    writer.print("]");
    return this;
  }

  @Override
  public JsonStream numbers(Number[] values) {
    return numbers(Arrays.asList(values));
  }

  @Override
  public JsonStream numbers(List<? extends Number> values) {
    writer.print("[");
    int valueCount = 0;
    for (Number v : values) {
      if (valueCount > 0) {
        writer.print(",");
      }
      writer.print(v);
      valueCount++;
    }
    writer.print("]");
    return this;
  }

  @Override
  public JsonStream endArray() {
    if (!states.isEmpty()) {
      states.pop();
    }
    writer.print("]");
    return this;
  }

  @Override
  public JsonStream endObject() {
    if (!states.isEmpty()) {
      states.pop();
    }
    writer.println("}");
    return this;
  }
  
  /**
   * @param dateString a date string.
   * @return a new {@link Date}.
   * @throws ParseException if a parsing error occurs.
   */
  public static Date parseDate(String dateString) throws ParseException {
    synchronized (DATE_FORMAT) {
      return DATE_FORMAT.parse(dateString);
    }
  }
}
