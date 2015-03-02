package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.corus.client.common.Matcheable.CompositePattern;
import org.sapia.corus.client.common.Matcheable.Pattern;

public class MatcheableTest {

  @Test
  public void testDefaultPattern_matches_any() {
    Pattern  p = Matcheable.DefaultPattern.parse("*");
    assertTrue(p.matches("test"));
  }
  
  @Test
  public void testDefaultPattern_matches_exact() {
    Pattern  p = Matcheable.DefaultPattern.parse("test");
    assertTrue(p.matches("test"));
  }
  
  @Test
  public void testDefaultPattern_matches_exact_false() {
    Pattern  p = Matcheable.DefaultPattern.parse("test1");
    assertFalse(p.matches("test"));
  }
  
  @Test
  public void testCompositePattern_matches_any() {
    Pattern  p1 = Matcheable.DefaultPattern.parse("test1");
    Pattern  p2 = Matcheable.DefaultPattern.parse("test2");
    assertTrue(CompositePattern.newInstance().any().add(p1, p2).matches("test2"));
  }
  
  @Test
  public void testCompositePattern_matches_any_false() {
    Pattern  p1 = Matcheable.DefaultPattern.parse("test3");
    Pattern  p2 = Matcheable.DefaultPattern.parse("test4");
    assertFalse(CompositePattern.newInstance().any().add(p1, p2).matches("test2"));
  }

  @Test
  public void testCompositePattern_matches_all() {
    Pattern  p1 = Matcheable.DefaultPattern.parse("tes*");
    Pattern  p2 = Matcheable.DefaultPattern.parse("*est");
    assertTrue(CompositePattern.newInstance().add(p1, p2).all().matches("test"));
  }
  
  @Test
  public void testCompositePattern_matches_all_false() {
    Pattern  p1 = Matcheable.DefaultPattern.parse("tes*");
    Pattern  p2 = Matcheable.DefaultPattern.parse("test1");
    assertFalse(CompositePattern.newInstance().add(p1, p2).all().matches("test"));
  }

  
}
