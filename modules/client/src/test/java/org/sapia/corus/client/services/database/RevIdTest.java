package org.sapia.corus.client.services.database;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.corus.client.services.database.RevId;

public class RevIdTest {

  @Test
  public void testValueOf() {
    assertEquals("test", RevId.valueOf("test").get());
  }

  @Test
  public void testEquals() {
    RevId r1 = RevId.valueOf("r1");
    RevId r2 = RevId.valueOf("r1");
    assertEquals(r1, r1);
    assertEquals(r1, r2);
  }
  
  @Test
  public void testEquals_false() {
    RevId r1 = RevId.valueOf("r1");
    RevId r2 = RevId.valueOf("r2");
    assertNotEquals(r1, r2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalid_no_alphanumeric() {
    RevId.valueOf("-_.");
  }
  
  @Test
  public void testValueOf_underscore() {
    RevId.valueOf("_valid");
  }
  
  @Test
  public void testValueOf_dash() {
    RevId.valueOf("-valid");
  }
  
  @Test
  public void testValueOf_dot() {
    RevId.valueOf(".valid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOf_invalid_characters() {
    RevId.valueOf("invalid!");
  }
}
