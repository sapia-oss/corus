package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ObjectUtilsTest {
  
  String value1, value2;
  
  @Before
  public void setUp() {
    value1 = "value1";
    value2 = "value2";
  }

  @Test
  public void testSafeHashCode() {
    assertNotSame(0, ObjectUtils.safeHashCode(value1));
  }
  
  @Test
  public void testSafeHashCodeNullObject() {
    assertEquals(0, ObjectUtils.safeHashCode((Object) null));
  }

  @Test
  public void testSafeHashCodeArray() {
    assertNotSame(0, ObjectUtils.safeHashCode(value1, value2));
  }
  
  @Test
  public void testSafeHashCodeArrayNullObjects() {
    assertEquals(0, ObjectUtils.safeHashCode(null, null));
  }
  
  @Test
  public void testSafeHashCodeArraySomeNullObjects() {
    assertNotSame(0, ObjectUtils.safeHashCode(value1, null));
  }

}
