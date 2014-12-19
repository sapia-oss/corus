package org.sapia.corus.client.common.rest;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.util.Collects;

public class ValueTest {

  @Test
  public void testGetName() {
    Value v = new Value("test", null);
    assertEquals("test", v.getName());
  }

  @Test
  public void testNotNull() {
    Value v = new Value("test", "v");
    v.notNull();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNotNull_throws_exception() {
    Value v = new Value("test", null);
    v.notNull();
  }

  @Test
  public void testIsNull() {
    Value v = new Value("test", null);
    assertTrue(v.isNull());
  }
  
  @Test
  public void testIsNull_false() {
    Value v = new Value("test", "v");
    assertFalse(v.isNull());
  }

  @Test
  public void testIsSet() {
    Value v = new Value("test", "v");
    assertTrue(v.isSet());  
  }
  
  @Test
  public void testIsSet_false() {
    Value v = new Value("test", null);
    assertFalse(v.isSet());  
  }

  @Test
  public void testAsBoolean_false_string() {
    Value v = new Value("test", "false");
    assertFalse(v.asBoolean());
  }
  
  @Test
  public void testAsBoolean_null() {
    Value v = new Value("test", null);
    assertFalse(v.asBoolean());
  }

  @Test
  public void testAsBoolean_true_string() {
    Value v = new Value("test", "true");
    assertTrue(v.asBoolean());
  }
  
  @Test
  public void testAsBoolean_on() {
    Value v = new Value("test", "on");
    assertTrue(v.asBoolean());
  }
  
  @Test
  public void testAsBoolean_1_string() {
    Value v = new Value("test", "1");
    assertTrue(v.asBoolean());
  }
  
  @Test
  public void testAsBoolean_yes() {
    Value v = new Value("test", "yes");
    assertTrue(v.asBoolean());
  }
  
  @Test
  public void testAsInt() {
    Value v = new Value("test", "1");
    assertEquals(1, v.asInt());
  }

  @Test
  public void testAsSet() {
    Value v = new Value("test", "1,2,3");
    Set<String> values = v.asSet();
    assertEquals(3, values.size());
    assertTrue(values.containsAll(Collects.arrayToSet("1", "2", "3")));
  }

  @Test
  public void testAsList() {
    Value v = new Value("test", "1,2,3");
    List<String> values = v.asList();
    assertEquals(3, values.size());
    assertTrue(values.containsAll(Collects.arrayToList("1", "2", "3")));
  }

}
