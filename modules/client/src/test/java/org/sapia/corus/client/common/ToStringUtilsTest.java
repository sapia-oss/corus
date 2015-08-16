package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ToStringUtilsTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testJoinToString_with_delim() {
    assertEquals("1, 2, 3", ToStringUtils.joinToString(",", new Integer(1), new Integer(2), new Integer(3)));
  }

  @Test
  public void testJoinToString() {
    assertEquals("1, 2, 3", ToStringUtils.joinToString(new Integer(1), new Integer(2), new Integer(3)));
  }

}
