package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ParamContainerTest {
  
  private ParamContainer parent, child;

  @Before
  public void setUp() throws Exception {
    parent = new ParamContainer() {
    };
    
    child = new ParamContainer() {
    };
        
    child.setParent(parent);
  }

  @Test
  public void testAddParam() {
    parent.addParam("n1", "v1");
    assertEquals(1, parent.getParams().size());
  }
  
  @Test
  public void testAddParam_with_child() {
    parent.addParam("n1", "v1");
    child.addParam("n2", "v2");

    assertEquals(1, parent.getParams().size());
    assertEquals(2, child.getParams().size());
  }

  @Test
  public void testGetSpecificParams() {
    child.addParam("n1", "v1");
    assertEquals(1, child.getSpecificParams().size());
  }

  @Test
  public void testAddParams() {
    child.addParams(Arrays.asList(Param.of("n1", "v1"), Param.of("n2", "v2")));
    assertEquals(2, child.getParams().size());
  }

  @Test
  public void testGetParam() {
    parent.addParam("n1", "v1");
    assertEquals("v1", parent.getParam("n1").getValue());
  }
  
  @Test
  public void testGetParam_with_child() {
    parent.addParam("n1", "v1");
    assertEquals("v1", child.getParam("n1").getValue());
  }

  @Test
  public void testGetParam_with_default_value() {
    assertEquals("v1", parent.getParam("n1", "v1").getValue());
  }
  
  @Test
  public void testGetParam_with_child_and_default_value() {
    assertEquals("v1", parent.getParam("n1", "v1").getValue());
  }
  
  @Test
  public void testGetParam_with_parent_child_and_default_value() {
    parent.addParam("n1", "v1");
    assertEquals("v1", parent.getParam("n1", "v2").getValue());
  }

  @Test
  public void testExistsParam() {
    parent.addParam("n1", "v1");
    assertTrue(parent.existsParam("n1"));
  }

  @Test
  public void testExistsParam_with_child() {
    parent.addParam("n1", "v1");
    assertTrue(child.existsParam("n1"));
  }
}
