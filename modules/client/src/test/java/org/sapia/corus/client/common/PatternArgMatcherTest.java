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
  public void testMatches_not() {
    PatternArgMatcher matcher = new PatternArgMatcher("!foo*");

    assertFalse(matcher.matches("foobar"));
  }

  @Test
  public void testEqualsObject() {
    PatternArgMatcher matcher1 = new PatternArgMatcher("foo*");
    PatternArgMatcher matcher2 = new PatternArgMatcher("foo*");

    assertEquals(matcher1, matcher2);
  }

  @Test
  public void testEqualsObject_not() {
    PatternArgMatcher matcher1 = new PatternArgMatcher("!foo*");
    PatternArgMatcher matcher2 = new PatternArgMatcher("!foo*");

    assertEquals(matcher1, matcher2);
  }
}
