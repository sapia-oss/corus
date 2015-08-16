package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.Arrays;

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

  @Test
  public void testSafeNonNull_first_null() {
    assertEquals("test", ObjectUtils.safeNonNull(null, "test"));
  }

  @Test
  public void testSafeNonNull_first_not_null() {
    assertEquals("test", ObjectUtils.safeNonNull("test", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSafeNonNull_all_null() {
    ObjectUtils.safeNonNull(null, null);
  }
  
  @Test
  public void testSafeListEquals() {
    assertTrue(ObjectUtils.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "3")));
  }

  @Test
  public void testSafeListEquals_not_same_content() {
    assertFalse(ObjectUtils.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2", "4")));
  }

  @Test
  public void testSafeListEquals_not_same_size() {
    assertFalse(ObjectUtils.safeListEquals(Arrays.asList("1", "2", "3"), Arrays.asList("1", "2")));
  }
}
