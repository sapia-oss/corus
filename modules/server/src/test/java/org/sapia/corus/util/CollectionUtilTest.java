package org.sapia.corus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.util.Condition;

public class CollectionUtilTest {

  @Test
  public void testFilterToArrayList() {
    Set<Integer> values = new HashSet<Integer>();
    values.add(1);
    values.add(2);
    values.add(3);
    values.add(4);
    List<Integer> result = CollectionUtil.filterToArrayList(values, new Condition<Integer>() {
      public boolean apply(Integer item) {
        return item.intValue() % 2 == 0;
        
      }
    });
    
    assertEquals(2, result.size());
    assertTrue(values.contains(new Integer(2)));
    assertTrue(values.contains(new Integer(4)));
    
  }

}
