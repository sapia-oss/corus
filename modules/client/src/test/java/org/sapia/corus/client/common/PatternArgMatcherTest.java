package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class PatternArgMatcherTest {
  
  @Test
  public void testMatches() {
    PatternArgMatcher matcher = new PatternArgMatcher("foo*");

    assertTrue(matcher.matches("foobar"));
  }
  
  @Test
  public void testMatches_with_plus() {
    PatternArgMatcher matcher = new PatternArgMatcher("foo+");

    assertTrue(matcher.matches("foobar"));
  }
  
  @Test
  public void testMatches_not() {
    PatternArgMatcher matcher = new PatternArgMatcher("!foo*");

    assertFalse(matcher.matches("foobar"));
  }
  
  @Test
  public void testMatches_no_with_plus() {
    PatternArgMatcher matcher = new PatternArgMatcher("!foo+");

    assertFalse(matcher.matches("foobar"));
  }


  @Test
  public void testEquals() {
    PatternArgMatcher matcher1 = new PatternArgMatcher("foo*");
    PatternArgMatcher matcher2 = new PatternArgMatcher("foo*");

    assertEquals(matcher1, matcher2);
  }

  @Test
  public void testEquals_not() {
    PatternArgMatcher matcher1 = new PatternArgMatcher("!foo*");
    PatternArgMatcher matcher2 = new PatternArgMatcher("!foo*");

    assertEquals(matcher1, matcher2);
  }
}
