package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Test;

public class CorusPropertyTest {

  @Test
  public void testEqualsObject() {
    CorusProperty p1 = new CorusProperty();
    p1.setName("p1");
    p1.setValue("v1");

    CorusProperty p2 = new CorusProperty();
    p2.setName("p2");
    p2.setValue("v2");

    CorusProperty p3 = new CorusProperty();
    p3.setName("p1");
    p3.setValue("v1");
    
    assertNotEquals(p1, p2);
    assertEquals(p1, p3);
  }

}
