package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParamTest {

  @Test
  public void testEqualsObject() {
    Param p1 = new Param();
    p1.setName("p1");
    p1.setValue("v1");

    Param p2 = new Param();
    p2.setName("p2");
    p2.setValue("v2");

    Param p3 = new Param();
    p3.setName("p1");
    p3.setValue("v1");
    
    assertNotEquals(p1, p2);
    assertEquals(p1, p3);
  }

}
