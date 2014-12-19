package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

public class PairTupleTest {
  
  private PairTuple<String, Integer> kv1, kv1_b, kv2;
  
  @Before
  public void setUp() {
    kv1   = new PairTuple<String, Integer>("k1", 1);
    kv1_b = new PairTuple<String, Integer>("k1", 1);
    kv2   = new PairTuple<String, Integer>("k2", 2);
  }
  
  @Test
  public void testHashCode() {
    assertEquals(kv1.hashCode(), kv1_b.hashCode());
  }


  @Test
  public void testGetKey() {
    assertEquals("k1", kv1.getLeft());
  }

  @Test
  public void testGetValue() {
    assertEquals(new Integer(1), kv1.getRight());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSerialization() {
    PairTuple<String, Integer> copy = (PairTuple<String, Integer>) SerializationUtils.deserialize(SerializationUtils.serialize(kv1));
    assertEquals(kv1, copy);
  }

  @Test
  public void testEqualsObject() {
    assertEquals(kv1, kv1_b);
  }

  @Test
  public void testEqualsObject_false() {
    assertNotSame(kv1, kv2);
  }

}
