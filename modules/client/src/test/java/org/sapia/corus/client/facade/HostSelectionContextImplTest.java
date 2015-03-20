package org.sapia.corus.client.facade;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost;

public class HostSelectionContextImplTest {

  private HostSelectionContextImpl context;
  
  
  @Before
  public void setUp() {
    context = new HostSelectionContextImpl();
    context.push(new ArrayList<CorusHost>());
  }
  
  @Test
  public void testPop() {
    assertFalse(context.pop().isNull());
  }

  @Test
  public void testPop_null() {
    context.pop();
    assertTrue(context.pop().isNull());
  }
  
  @Test
  public void testPeek() {
    assertFalse(context.peek().isNull());
  }
  
  @Test
  public void testPeek_null() {
    context.pop();
    assertTrue(context.peek().isNull());
  }


}
