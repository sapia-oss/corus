package org.sapia.corus.client.common.reference;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

public class ImmutableReferenceTest {

  private ImmutableReference<Integer> ref, ref2;

  @Before
  public void setUp() throws Exception {
    ref  = new ImmutableReference<Integer>(1);
    ref2 = new ImmutableReference<Integer>(2);
  }

  @Test
  public void testGet() {
    assertEquals(new Integer(1), ref.get());
  }
  
  @Test
  public void testSet() {
    ref.set(5);
    assertEquals(new Integer(1), ref.get());
  }
  
  @Test
  public void testSetIf() {
    ref.setIf(5, 1);
    assertEquals(new Integer(1), ref.get());
  }

  @Test
  public void testOf() {
    assertEquals(new Integer(1), new ImmutableReference<Integer>(new Integer(1)).get());
  }

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(ref);
    ImmutableReference<Integer> copy = (ImmutableReference<Integer>) SerializationUtils.deserialize(payload);
    
    assertEquals(ref, copy);
  }

  @Test
  public void testEquals() {
    assertEquals(ref, ref);
  }

  @Test
  public void testEquals_false() {
    assertNotEquals(ref, ref2);
  }

}
