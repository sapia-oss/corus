package org.sapia.corus.client.common.rest;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;

public class PathTemplateTest {

  @Test
  public void testMatches() {
    PathTemplate t = PathTemplate.parse("/path1/{value1}");
    MatchResult result = t.matches("/path1/1234");
    assertTrue(result.matched());
    assertEquals("1234", result.getValues().get("value1"));
  }
  
  @Test
  public void testMatches_complex_path() {
    PathTemplate t = PathTemplate.parse("/path1/{value1}/path2/{value2}");
    MatchResult result = t.matches("/path1/1234/path2/5678");
    assertTrue(result.matched());
    assertEquals("1234", result.getValues().get("value1"));
    assertEquals("5678", result.getValues().get("value2"));
  }
  
  @Test
  public void testMatches_false() {
    PathTemplate t = PathTemplate.parse("/path1/{value1}/foo");
    MatchResult result = t.matches("/path1/1234");
    assertFalse(result.matched());
  }
  
  @Test
  public void testCompareTo_more_specific() {
    PathTemplate t1 = PathTemplate.parse("/path1/{value1}/foo");
    PathTemplate t2 = PathTemplate.parse("/path1/{value1}/foo/bar");
    assertTrue(t2.compareTo(t1) < 0);
  }
  
  @Test
  public void testCompareTo_less_specific() {
    PathTemplate t2 = PathTemplate.parse("/path1/{value1}/foo");
    PathTemplate t1 = PathTemplate.parse("/path1/{value1}/foo/bar");
    assertTrue(t2.compareTo(t1) > 0);
  }

  @Test
  public void testCompareTo_is_equal() {
    PathTemplate t2 = PathTemplate.parse("/path1/{value1}/foo");
    PathTemplate t1 = PathTemplate.parse("/path1/{value1}/foo");
    assertTrue(t2.compareTo(t1) == 0);
  }

  @Test
  public void testEquals() {
    PathTemplate t1 = PathTemplate.parse("/path1/{value1}/foo");
    PathTemplate t2 = PathTemplate.parse("/path1/{value1}/foo");
    assertEquals(t1, t2);
  }
  
  @Test
  public void testEquals_false() {
    PathTemplate t1 = PathTemplate.parse("/path1/{value1}/foo");
    PathTemplate t2 = PathTemplate.parse("/path1/{value1}/foo");
    assertNotSame(t1, t2);
  }
  
  @Test
  public void testToString() {
    PathTemplate t = PathTemplate.parse("/path1/{value1}/foo");
    assertEquals("/path1/{value1}/foo", t.toString());
  }
}
