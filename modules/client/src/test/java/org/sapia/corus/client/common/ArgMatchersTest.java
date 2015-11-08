package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ArgMatchersTest {


  @Test
  public void testParse_pattern() {
    ArgMatcher m = ArgMatchers.parse("f*");
    assertTrue(m.matches("foo"));
  }
  
  @Test
  public void testParse_alternate_pattern() {
    ArgMatcher m = ArgMatchers.parse("f+");
    assertTrue(m.matches("foo"));
  }

}
