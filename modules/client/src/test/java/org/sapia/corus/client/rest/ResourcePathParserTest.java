package org.sapia.corus.client.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResourcePathParserTest {

  @Before
  public void setUp() throws Exception {
  }
  
  @Test
  public void testParse_normal_path() {
    String[] path = ResourcePathParser.parse("p1/p2/p3");
    assertEquals(3, path.length);
    assertEquals("p1", path[0]);
    assertEquals("p2", path[1]);    
    assertEquals("p3", path[2]);    
  }

  @Test
  public void testParse_brackets_beginning() {
    String[] path = ResourcePathParser.parse("[p1/p2]/p3/p4");
    assertEquals(3, path.length);
    assertEquals("p1/p2", path[0]);
  }
  
  @Test
  public void testParse_brackets_middle() {
    String[] path = ResourcePathParser.parse("p1/[p2/p3]/p4");
    assertEquals(3, path.length);
    assertEquals("p2/p3", path[1]);
  }

  @Test
  public void testParse_brackets_end() {
    String[] path = ResourcePathParser.parse("p1/p2/[p3/p4]");
    assertEquals(3, path.length);
    assertEquals("p3/p4", path[2]);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParse_double_opening_brackets() {
    ResourcePathParser.parse("p1/[p2/[p3/p4");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParse_close_before_opening_bracket() {
    ResourcePathParser.parse("p1/]p2/p3/p4");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParse_unclosed_bracket() {
    ResourcePathParser.parse("p1/[p2/p3/p4");
  }
}
