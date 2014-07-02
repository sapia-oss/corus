package org.sapia.corus.util;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class CorusTimestampOutputStreamTest {

  private ByteArrayOutputStream delegate;
  private CorusTimestampOutputStream stream;

  private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  @Before
  public void setUp() throws Exception {
    delegate = new ByteArrayOutputStream();
    stream = new CorusTimestampOutputStream(delegate);
  }

  @Test
  public void testPrintln() {
    stream.println();
    assertDateFormat();
  }

  @Test
  public void testPrintlnBoolean() {
    stream.println(true);
    assertDateFormat();
  }

  @Test
  public void testPrintlnChar() {
    stream.println('c');
    assertDateFormat();
  }

  @Test
  public void testPrintlnInt() {
    stream.println(1);
    assertDateFormat();
  }

  @Test
  public void testPrintlnLong() {
    stream.println(1L);
    assertDateFormat();
  }

  @Test
  public void testPrintlnFloat() {
    stream.println(1f);
    assertDateFormat();
  }

  @Test
  public void testPrintlnDouble() {
    stream.println(1d);
    assertDateFormat();
  }

  @Test
  public void testPrintlnCharArray() {
    stream.println(new char[] {'c'});
    assertDateFormat();
  }

  @Test
  public void testPrintlnObject() {
    stream.println(new Object());
    assertDateFormat();
  }

  @Test
  public void testPrintlnString() {
    stream.println("test");
    assertDateFormat();
  }

  private void assertDateFormat() {
    String content = delegate.toString();
    String expected = format.format(new Date());
    assertTrue("Expected formatted content to hold: " +  expected + ". Got: " + content, content.contains(expected));
  }
}
