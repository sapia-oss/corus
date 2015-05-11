package org.sapia.corus.cloud.topology;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

public class XmlStreamWriterTest {

  private StringWriter    sw;
  private XmlStreamWriter writer;
  
  @Before
  public void setUp() {
    sw     = new StringWriter();
    writer = new XmlStreamWriter(sw, false);
  }
  
  @Test
  public void testEmptyElementNoAttributes() {
    String expected = "<element></element>";
   
    writer.beginRootElement("element");
    writer.endRootElement("element");
    
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testEmptyElementWithAttributes() {
    String expected = "<element attr1=\"val1\" attr2=\"val2\"></element>";
   
    writer.beginRootElement("element");
    writer.attribute("attr1", "val1");
    writer.attribute("attr2", "val2");
    writer.endRootElement("element");
    
    assertEquals(expected, sw.toString());
  }
  
  @Test
  public void testNonEmptyElementWithoutAttributes() {
    String expected = "<parent><child></child></parent>";
   
    writer.beginRootElement("parent");
    writer.beginElement("child");
    writer.endElement("child");
    writer.endRootElement("parent");
    
    assertEquals(expected, sw.toString());
  }
  
  @Test
  public void tesNonEmptyElementWithAttributes() {
    String expected = "<parent attr1=\"val1\" attr2=\"val2\"><child attr1=\"val1\" attr2=\"val2\"></child></parent>";
   
    writer.beginRootElement("parent");
    writer.attribute("attr1", "val1");
    writer.attribute("attr2", "val2");
    writer.beginElement("child");
    writer.attribute("attr1", "val1");
    writer.attribute("attr2", "val2");
    writer.endElement("child");
    writer.endRootElement("parent");
    
    assertEquals(expected, sw.toString());
  }
  
  @Test
  public void testOutput() {
    sw     = new StringWriter();
    writer = new XmlStreamWriter(sw, true);
    writer.beginRootElement("parent");
    writer.attribute("attr1", "val1");
    writer.attribute("attr2", "val2");
    writer.beginElement("child");
    writer.attribute("attr1", "val1");
    writer.attribute("attr2", "val2");
    writer.endElement("child");
    writer.endRootElement("parent");
    System.out.println(sw.toString());
  }
  
}
