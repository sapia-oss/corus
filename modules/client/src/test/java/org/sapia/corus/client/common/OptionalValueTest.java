package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

public class OptionalValueTest {
  
  private OptionalValue<String> val1, val2, equal, nullVal;
  
  
  @Before
  public void setUp() {
    val1    = OptionalValue.of("1");
    equal   = OptionalValue.of("1");
    val2    = OptionalValue.of("2");
    nullVal = OptionalValue.none();
  }

  @Test
  public void testExternalization() {
    OptionalValue<String> copy = (OptionalValue<String>) SerializationUtils.deserialize(SerializationUtils.serialize(val1));
    assertEquals(val1, copy);
  }

  @Test
  public void testIsNull() {
    assertTrue(nullVal.isNull());
    assertFalse(nullVal.isSet());
  }

  @Test
  public void testIsSet() {
    assertTrue(val1.isSet());
    assertFalse(val1.isNull());
  }

  @Test
  public void testGet() {
    assertEquals(val1.get(), "1");
  }

  @Test
  public void testEquals() {
    assertEquals(val1, equal);
  }
  
  @Test
  public void testEquals_false() {
    assertNotEquals(val1, val2);
  }

  @Test
  public void testEquals_null() {
    assertEquals(val1, nullVal);
  } 
  
  @Test
  public void testHashcode() {
    assertEquals("1".hashCode(), val1.hashCode());
  }

  @Test
  public void testHashcode_null() {
    assertNotEquals("1".hashCode(), nullVal.hashCode());
  }
}
