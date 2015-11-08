package org.sapia.corus.client.annotations;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.sapia.ubik.util.Collects;

public class PriorityLevelTest {

  @Test
  public void testComparator() {
    PriorityLevel p1 = PriorityLevel.LOW;
    PriorityLevel p2 = PriorityLevel.HIGH;
    assertTrue(PriorityLevel.comparator().compare(p2,  p1) < 0);
  }
  
  @Test
  public void testSortPriority() {
    Object o1 = new TestPrioritized1();
    Object o2 = new TestPrioritized2();
    Object o3 = new TestPrioritized3();
    
    List<Object> lst = Collects.arrayToList(o1, o3, o2);
    
    PriorityLevel.sort(lst);
    
    assertTrue(o2 == lst.get(0));
    assertTrue(o1 == lst.get(1));
    assertTrue(o3 == lst.get(2));
  }
  
  @Priority(PriorityLevel.LOW)
  class TestPrioritized1 {
    
  }
  
  @Priority(PriorityLevel.HIGH)
  class TestPrioritized2 {
    
  }
  
  class TestPrioritized3 {
    
  }

}
