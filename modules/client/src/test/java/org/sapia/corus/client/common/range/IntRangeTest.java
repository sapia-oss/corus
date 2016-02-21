package org.sapia.corus.client.common.range;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Serialization;

public class IntRangeTest {
  
  private IntRange range;

  @Before
  public void setUp() throws Exception {
    range = new IntRange(0, 9);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIntRange_invalid_args() {
    new IntRange(9, 0);
  }

  @Test
  public void testGetMin() {
    assertEquals(0, range.getMin().intValue());
  }

  @Test
  public void testGetMax() {
    assertEquals(9, range.getMax().intValue());
  }

  @Test
  public void testIsWithin() {
    assertTrue(range.isWithin(0));
    assertTrue(range.isWithin(1));
    assertTrue(range.isWithin(9));
  }
  
  @Test
  public void testIsOutside() {
    assertFalse(range.isWithin(-1));
    assertFalse(range.isWithin(10));
  }
  
  @Test
  public void testLength() {
    assertEquals(10, range.length());
  }

  @Test
  public void testAsList() {
    assertEquals(10, range.asList().size());
    assertTrue(Arrays.asList(0, 1 , 2, 3, 4, 5, 6, 7, 8, 9).containsAll(range.asList()));
  }

  @Test
  public void testAsList_with_function() {
    List<String> values =  range.asList(new Func<String, Integer>() {
      @Override
      public String call(Integer value) {
        return value.toString();
      }
    });
    assertEquals(10, values.size());
    assertTrue(Arrays.asList("0", "1" , "2", "3", "4", "5", "6", "7", "8", "9").containsAll(values));
  }
  
  @Test
  public void testForEach() {
    final List<Integer> items = new ArrayList<>(range.length());
    range.forEach(new Func<Void, Integer>() {
      @Override
      public Void call(Integer value) {
        items.add(value);
        return null;
      }
    });
    
    assertEquals(10, range.asList().size());
    assertTrue(Arrays.asList(0, 1 , 2, 3, 4, 5, 6, 7, 8, 9).containsAll(range.asList()));
  }

  @Test
  public void testSerialization() throws Exception {
    Range<Integer> copy = (Range<Integer>) Serialization.deserialize(Serialization.serialize(range));
    assertEquals(range, copy);
  }
  
  @Test
  public void testEquals() {
    Range<Integer> other = new IntRange(0, 9);
    assertEquals(range, other);
  }

  @Test
  public void testEquals_false() {
    Range<Integer> other = new IntRange(1, 9);
    assertNotEquals(range, other);
  }
 
  @Test
  public void testForLength() {
    Range<Integer> other = IntRange.forLength(10);
    assertEquals(range, other);
  }
  
  @Test
  public void testForLength_with_min() {
    Range<Integer> other = IntRange.forLength(0, 10);
    assertEquals(range, other);
  }
  
  @Test
  public void testForBounds() {
    Range<Integer> other = IntRange.forBounds(0, 9);
    assertEquals(range, other);
  }

}
