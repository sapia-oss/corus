package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Condition;

public class CircularBufferTest {
  
  private CircularBuffer<Integer> buffer;

  @Before
  public void setUp() throws Exception {
    buffer = new CircularBuffer<Integer>(5);
    for (int i = 0; i < 5; i++) {
      buffer.add(new Integer(i));
    }
  }

  @Test
  public void testAdd_within_capacity() {
    
    assertEquals(5, buffer.size());

    List<Integer> lst = buffer.asList();
    for (int i = 0; i < 5; i++) {
      assertEquals(new Integer(i), lst.get(i));
    }
  }

  @Test
  public void testAdd_beyond_capacity() {
    buffer.add(new Integer(5));
    
    assertEquals(5, buffer.size());
    
    List<Integer> lst = buffer.asList();
    for (int i = 0; i < 5; i++) {
      assertEquals(new Integer(i + 1), lst.get(i));
    }
  }
  
  @Test
  public void testSubsList() {
    List<Integer> lst = buffer.subList(new Condition<Integer>() {
      @Override
      public boolean apply(Integer item) {
        return item.intValue() < 3;
      }
    });
    
    for (int i = 0; i < 3; i++) {
      assertEquals(new Integer(i), lst.get(i));
    }
  }

  @Test
  public void testClear_with_condition() {
    List<Integer> lst = buffer.clear(new Condition<Integer>() {
      @Override
      public boolean apply(Integer item) {
        return item.intValue() < 3;
      }
    });

    for (int i = 0; i < 3; i++) {
      assertEquals(new Integer(i), lst.get(i));
    }
    
    lst = buffer.asList();
    
    assertEquals(new Integer(3), lst.get(0));
    assertEquals(new Integer(4), lst.get(1));
  }

  @Test
  public void testClear() {
    buffer.clear();
    assertTrue(buffer.size() == 0);
  }

  @Test
  public void testIterator() {
    List<Integer> lst = new ArrayList<Integer>();
    for (Integer i : buffer) {
      lst.add(i);
    }
    
    for (int i = 0; i < 5; i++) {
      assertEquals(new Integer(i), lst.get(i));
    }
  }

}
