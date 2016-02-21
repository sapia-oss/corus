package org.sapia.corus.client.common.tuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class TripleTupleTest {
  
  private TripleTuple<String, Integer, Long> triple;

  @Before
  public void setUp() throws Exception {
    triple = new TripleTuple<String, Integer, Long>("1", new Integer(2), new Long(3));
  }

  @Test
  public void testGetFirst() {
    assertEquals("1", triple.getFirst());
  }

  @Test
  public void testGetSecond() {
    assertEquals(new Integer(2), triple.getSecond());
  }

  @Test
  public void testGetThird() {
    assertEquals(new Long(3), triple.getThird());
  }

  @Test
  public void testSerialization() throws Exception {
    TripleTuple<String, Integer, Long> copy = (TripleTuple<String, Integer, Long>) Serialization.deserialize(Serialization.serialize(triple));
    assertEquals(triple, copy);
  }

  @Test
  public void testEquals() {
    TripleTuple<String, Integer, Long> other = new TripleTuple<>("1", new Integer(2), new Long(3));
    assertEquals(triple, other);
  }

  @Test
  public void testEquals_false() {
    TripleTuple<String, Integer, Long> other = new TripleTuple<>("2", new Integer(2), new Long(3));
    assertNotEquals(triple, other);
  }
}
