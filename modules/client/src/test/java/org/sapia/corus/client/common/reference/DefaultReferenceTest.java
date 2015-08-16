package org.sapia.corus.client.common.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

public class DefaultReferenceTest {
  
  private DefaultReference<Integer> ref, ref2;

  @Before
  public void setUp() throws Exception {
    ref  = new DefaultReference<Integer>(1);
    ref2 = new DefaultReference<Integer>(2);
  }

  @Test
  public void testGet() {
    assertEquals(new Integer(1), ref.get());
  }
  
  @Test
  public void testSet() {
    ref.set(5);
    assertEquals(new Integer(5), ref.get());
  }

  @Test
  public void testOf() {
    assertEquals(new Integer(1), DefaultReference.of(new Integer(1)).get());
  }

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(ref);
    DefaultReference<Integer> copy = (DefaultReference<Integer>) SerializationUtils.deserialize(payload);
    
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
