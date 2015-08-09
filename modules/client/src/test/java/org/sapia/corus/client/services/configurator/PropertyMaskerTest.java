package org.sapia.corus.client.services.configurator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.configurator.PropertyMasker;

public class PropertyMaskerTest {

  PropertyMasker mask;
  
  @Before
  public void setUp() throws Exception {
    mask = new PropertyMasker();
  }

  @Test
  public void testAddMatcher_patterns() {
    mask.addMatcher("*test*");
    assertTrue(mask.isHidden("some.test.property"));
  }

  @Test
  public void testAddMatcher() {
    mask.addMatcher(ArgMatchers.parse("*test*"));
    assertTrue(mask.isHidden("some.test.property"));
  }

  @Test
  public void testIsHidden_false() {
    mask.addMatcher(ArgMatchers.parse("*test*"));
    assertFalse(mask.isHidden("some.property"));
  }
  
  @Test
  public void testIsHidden_multiple_matchers() {
    mask.addMatcher(ArgMatchers.parse("*test1*"), ArgMatchers.parse("*test2*"));
    assertTrue(mask.isHidden("some.test1.property"));
    assertTrue(mask.isHidden("some.test2.property"));
    assertFalse(mask.isHidden("some.property"));
  }

  @Test
  public void testIsHidden_empty_matchers() {
    assertFalse(mask.isHidden("some.property"));
  }
  
  @Test
  public void testGetMaskedValue() {
    mask.addMatcher("*test*");
    assertEquals(PropertyMasker.DEFAULT_MASK, mask.getMaskedValue("test", "testValue"));
  }
  
  @Test
  public void testGetMaskedProperty() {
    mask.addMatcher("*test*");
    assertEquals(PropertyMasker.DEFAULT_MASK, mask.getMaskedProperty(new Property("test", "testValue")).getValue());
  }
}

