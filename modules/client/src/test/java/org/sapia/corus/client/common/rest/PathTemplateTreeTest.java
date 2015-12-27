package org.sapia.corus.client.common.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.rest.PathTemplate.MatchResult;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.ubik.util.Condition;

public class PathTemplateTreeTest {
  
  private PathTemplateTree<String> tree;


  @Before
  public void setUp() throws Exception {
    tree = new PathTemplateTree<>();
  }
  
  private Condition<String> select(final String selector) {
    return new Condition<String>() {
      @Override
      public boolean apply(String value) {
        return selector.equals(value);
      }
    };
  }

  @Test
  public void testMatches_simple_template_without_variable() {
    tree.addTemplate(PathTemplate.parse("p1"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(0, r.getLeft().getValues().size());
    assertEquals("test", r.getRight().get());
  }
  
  @Test
  public void testMatches_simple_template_with_variable() {
    tree.addTemplate(PathTemplate.parse("{v1}"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(1, r.getLeft().getValues().size());
    assertEquals("p1", r.getLeft().getValues().get("v1"));
    assertEquals("test", r.getRight().get());
  }

  @Test
  public void testMatches_complex_template_without_variable() {
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2", select("test"));

    assertTrue(r.getLeft().matched());
    assertEquals(0, r.getLeft().getValues().size());
    assertEquals("test", r.getRight().get());
  }
  
  @Test
  public void testMatches_complex_template_with_variable() {
    tree.addTemplate(PathTemplate.parse("{v1}/{v2}"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(2, r.getLeft().getValues().size());
    assertEquals("p1", r.getLeft().getValues().get("v1"));
    assertEquals("p2", r.getLeft().getValues().get("v2"));
    assertEquals("test", r.getRight().get());
  }
  
  @Test
  public void testMatches_exact_match() {
    tree.addTemplate(PathTemplate.parse("p1/p3"), "test");
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(0, r.getLeft().getValues().size());
    assertEquals("test", r.getRight().get());
  }
  
  @Test
  public void testMatches_variable_match() {
    tree.addTemplate(PathTemplate.parse("p1/{v3}"), "test");
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p3", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(1, r.getLeft().getValues().size());
    assertEquals("p3", r.getLeft().getValues().get("v3"));
    assertEquals("test", r.getRight().get());
  }
  
  @Test
  public void testMatches_variable_mismatch() {
    tree.addTemplate(PathTemplate.parse("p1/{v3}"), "test");
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2", select("test"));
    
    assertTrue(r.getLeft().matched());
    assertEquals(0, r.getLeft().getValues().size());
    assertEquals("test", r.getRight().get());
  }
 
  @Test
  public void testMatches_multi_value() {
    tree.addTemplate(PathTemplate.parse("p1/p2/p3"), "test1");
    tree.addTemplate(PathTemplate.parse("p1/p2/p3"), "test2");
    tree.addTemplate(PathTemplate.parse("p1/p2/p3"), "test3");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2/p3", select("test3"));
    
    assertTrue(r.getLeft().matched());
    assertEquals("test3", r.getRight().get());
  }
  
  @Test
  public void testMatches_multi_value_no_match() {
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test1");
    tree.addTemplate(PathTemplate.parse("p1/p2"), "test2");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("p1/p2", select("test3"));
    
    assertFalse(r.getLeft().matched());
  }
  
  @Test
  public void testMatches_cluster() {
    tree.addTemplate(PathTemplate.parse("/clusters/{corus:cluster}/hosts/{corus:host}/distributions"), "test1");
    tree.addTemplate(PathTemplate.parse("/clusters/{corus:cluster}/hosts/distributions"), "test2");
    tree.addTemplate(PathTemplate.parse("/clusters/{corus:cluster}/hosts/distributions"), "test3");
    tree.addTemplate(PathTemplate.parse("/clusters/{corus:cluster}/hosts/distributions"), "test4");
    
    PairTuple<MatchResult, OptionalValue<String>> r = tree.matches("/clusters/ftest/hosts/distributions", select("test3"));
    
    assertTrue(r.getLeft().matched());
    assertEquals("test3", r.getRight().get());
  }
}
