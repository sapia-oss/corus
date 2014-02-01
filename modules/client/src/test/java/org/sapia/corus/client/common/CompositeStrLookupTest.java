package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.apache.commons.lang.text.StrLookup;
import org.junit.Before;
import org.junit.Test;

public class CompositeStrLookupTest {
  
  private CompositeStrLookup lookup;
  
  @Before
  public void setUp() {
    lookup = new CompositeStrLookup();
  }

  @Test
  public void testLenient() {
    lookup.lenient();
    assertEquals("${test}", lookup.lookup("test"));
  }

  @Test
  public void testLookup() {
    lookup.add(StrLookup.systemPropertiesLookup());
    assertEquals(System.getProperty("user.name"), lookup.lookup("user.name"));
  }

}
