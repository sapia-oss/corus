package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class NameValuePairTest {
  
  private NameValuePair np1, np2, np3;
 
  @Before
  public void setUp() {
    np1 = new NameValuePair("np", "np1");
    np2 = new NameValuePair("np", "np2");
    np3 = new NameValuePair("np3", "np3");

  }

  @Test
  public void testCompareTo() {
    assertTrue(np1.compareTo(np3) < 0);
    assertTrue(np3.compareTo(np1) > 0);
    assertTrue(np1.compareTo(np1) == 0);
  }

  @Test
  public void testMatches() {
    assertTrue(np1.matches(Matcheable.DefaultPattern.parse("np*")));
  }

  @Test
  public void testEquals() {
    assertEquals(np1, np1);
  }

  @Test
  public void testEqualsObject_false() {
    assertNotSame(np1, np2);
  }
}
