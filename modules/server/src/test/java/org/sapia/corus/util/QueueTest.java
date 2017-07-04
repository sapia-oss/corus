package org.sapia.corus.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class QueueTest {

  Queue<String> queue;
  
  @Before
  public void setUp() throws Exception {
    queue = new Queue<>();
  }

  @Test
  public void testAdd() {
    queue.add("test");
    
    assertEquals(1, queue.size());
  }

  @Test
  public void testRemoveAll() {
    queue.add("1", "2");
    
    queue.removeAll();
    
    assertEquals(0, queue.size());
  }

  @Test
  public void testRemoveFirst() {
    queue.add("1", "2");
    
    assertEquals("1", queue.removeFirst());
  }

  @Test
  public void testRemoveLast() {
    queue.add("1", "2");
    
    assertEquals("2", queue.removeLast());
  }

  @Test
  public void testSize() {
    queue.add("1", "2");
    
    assertEquals(2, queue.size());
  }

}
