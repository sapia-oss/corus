package org.sapia.corus.client.common.json;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class WriterJsonStreamTest {
  
  private StringWriter     delegate;
  private WriterJsonStream stream;
  
  @Before
  public void setUp() {
    delegate = new StringWriter();
    stream = new WriterJsonStream(delegate);
  }

  @Test
  public void testBeginObject() {
     stream.beginObject();
     assertEquals("{", delegate.getBuffer().toString());
  }

  @Test
  public void testBeginArray() {
    stream.beginArray();
    assertEquals("[", delegate.getBuffer().toString());
  }

  @Test
  public void testField() {
    stream.field("test");
    assertEquals("\"test\":", delegate.getBuffer().toString());
  }

  @Test
  public void testValueString() {
    stream.value("testValue");
    assertEquals("\"testValue\"", delegate.getBuffer().toString());  
  }

  @Test
  public void testValueNumber() {
    stream.value(1);
    assertEquals("1", delegate.getBuffer().toString());
  }

  @Test
  public void testValueDate() {
    Date now = new Date();
    stream.value(now);
    assertEquals("\"" + WriterJsonStream.DATE_FORMAT.format(now) + "\"", delegate.getBuffer().toString());
  }

  @Test
  public void testValueBoolean_true() {
    stream.value(true);
    assertEquals("true", delegate.getBuffer().toString());
  }

  @Test
  public void testValueBoolean_false() {
    stream.value(false);
    assertEquals("false", delegate.getBuffer().toString());
  }
  @Test
  public void testStrings_array() {
    stream.strings(new String[] {"1", "2", "3"});
    assertEquals("[\"1\",\"2\",\"3\"]", delegate.getBuffer().toString());
  }

  @Test
  public void testStrings_list() {
    stream.strings(Arrays.asList(new String[] {"1", "2", "3"}));
    assertEquals("[\"1\",\"2\",\"3\"]", delegate.getBuffer().toString());
  }

  @Test
  public void testNumbers_array() {
    stream.numbers(new Integer[] {1, 2, 3});
    assertEquals("[1,2,3]", delegate.getBuffer().toString());
  }

  @Test
  public void testNumbers_list() {
    stream.numbers(new Integer[] {1, 2, 3});
    assertEquals("[1,2,3]", delegate.getBuffer().toString());
  }

  @Test
  public void testEndArray() {
    stream.endArray();
    assertEquals("]", delegate.getBuffer().toString());
  }

  @Test
  public void testEndObject() {
    stream.endObject();
    assertEquals("}\n", delegate.getBuffer().toString());
  }

}
