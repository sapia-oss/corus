package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ObjectUtilTest {

  String value1, value2;

  @Before
  public void setUp() {
    value1 = "value1";
    value2 = "value2";
  }

  @Test
  public void testSafeHashCode() {
    assertNotSame(0, ObjectUtil.safeHashCode(value1));
  }

  @Test
  public void testSafeHashCodeNullObject() {
    assertEquals(0, ObjectUtil.safeHashCode((Object) null));
  }

  @Test
  public void testSafeHashCodeArray() {
    assertNotSame(0, ObjectUtil.safeHashCode(value1, value2));
  }

  @Test
  public void testSafeHashCodeArrayNullObjects() {
    assertEquals(0, ObjectUtil.safeHashCode(null, null));
  }

  @Test
  public void testSafeHashCodeArraySomeNullObjects() {
    assertNotSame(0, ObjectUtil.safeHashCode(value1, null));
  }

  @Test
  public void testSafeNonNull_first_null() {
    assertEquals("test", ObjectUtil.safeNonNull(null, "test"));
  }

  @Test
  public void testSafeNonNull_first_not_null() {
    assertEquals("test", ObjectUtil.safeNonNull("test", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSafeNonNull_all_null() {
    ObjectUtil.safeNonNull(null, null);
  }
  
  @Test
  public void testSafeListEquals() {
    assertTrue(ObjectUtil.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3")));
  }

  @Test
  public void testSafeListEquals_not_same_content() {
    assertFalse(ObjectUtil.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "4")));
  }

  @Test
  public void testSafeListEquals_not_same_size() {
    assertFalse(ObjectUtil.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2")));
  }
}
