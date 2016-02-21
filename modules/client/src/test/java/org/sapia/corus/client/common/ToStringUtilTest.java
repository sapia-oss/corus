package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ToStringUtilTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testJoinToString_with_delim() {
    assertEquals("1, 2, 3", ToStringUtil.joinToString(",", new Integer(1), new Integer(2), new Integer(3)));
  }

  @Test
  public void testJoinToString() {
    assertEquals("1, 2, 3", ToStringUtil.joinToString(new Integer(1), new Integer(2), new Integer(3)));
  }

  @Test
  public void testAbbreviate() {
    assertEquals("012...789", ToStringUtil.abbreviate("0123456789", 9, 3, 3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAbbreviate_invalid_num_start() {
    ToStringUtil.abbreviate("0123456789", 9, 10, 3);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAbbreviate_invalid_num_end() {
    ToStringUtil.abbreviate("0123456789", 9, 3, 10);
  }
  
}
