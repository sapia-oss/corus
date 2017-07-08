package org.sapia.corus.client.common.reference;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.SysClock.MutableClock;

public class AutoResetReferenceTest {
  
  private MutableClock clock;
  private AutoResetReference<String> ref;
  
  @Before
  public void setUp() throws Exception {
    clock = MutableClock.getInstance();
    ref = new AutoResetReference<String>("a", TimeValue.createMillis(1000));
    ref.setClock(clock);
  }

  @Test
  public void testSet() {
    ref.set("b");
    assertEquals("b", ref.get());
  }

  @Test
  public void testSetIf() {
    assertTrue(ref.setIf("b", "a"));
    assertEquals("b", ref.get());
  }
  
  @Test
  public void testSetIf_false() {
    assertFalse(ref.setIf("b", "c"));
    assertEquals("a", ref.get());
  }
  
  @Test
  public void testSetGet_delay_passed() {
    ref.set("b");
    clock.increaseCurrentTimeMillis(1001);
    assertEquals("a", ref.get());
  }
  
  @Test
  public void testSet_delay_passed_with_activity() {
    ref.set("b");
    clock.increaseCurrentTimeMillis(1001);
    ref.set("c");
    assertEquals("c", ref.get());
  }

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(ref);
    AutoResetReference<String> copy = (AutoResetReference<String>) SerializationUtils.deserialize(payload);
    
    assertEquals(ref, copy);
  }

  @Test
  public void testEquals() {
    AutoResetReference<String> ref2 = new AutoResetReference<String>("a", TimeValue.createMillis(1000));
    assertEquals(ref, ref2);
  }

  @Test
  public void testEquals_false() {
    AutoResetReference<String> ref2 = new AutoResetReference<String>("b", TimeValue.createMillis(1000));
    ref2.setClock(clock);
    assertNotEquals(ref, ref2);
  }

}
