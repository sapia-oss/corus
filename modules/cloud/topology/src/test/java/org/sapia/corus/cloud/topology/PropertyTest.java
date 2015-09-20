package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Test;

public class PropertyTest {

  @Test
  public void testEqualsObject() {
    Property p1 = new Property();
    p1.setName("p1");
    p1.setValue("v1");

    Property p2 = new Property();
    p2.setName("p2");
    p2.setValue("v2");

    Property p3 = new Property();
    p3.setName("p1");
    p3.setValue("v1");
    
    assertNotEquals(p1, p2);
    assertEquals(p1, p3);
  }

}
