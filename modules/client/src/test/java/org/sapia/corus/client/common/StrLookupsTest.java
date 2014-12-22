package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.text.StrLookup;
import org.junit.Test;

public class StrLookupsTest {

  @Test
  public void testForKeyValues() {
    StrLookup lk = StrLookups.forKeyValues("k", "v");
    assertEquals("v", lk.lookup("k"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForKeyValues_odd_input() {
    StrLookups.forKeyValues("k");
  }
  
  @Test
  public void testMerge() {
    StrLookup lk = StrLookups.merge(StrLookups.forKeyValues("k1", "v1"), StrLookups.forKeyValues("k2", "v2"));
    assertEquals("v1", lk.lookup("k1"));
    assertEquals("v2", lk.lookup("k2"));
  }

}
