package org.sapia.corus.client.services.configurator;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Serialization;

public class TagTest {
  
  private Tag t1, t2, t3;
  
  @Before
  public void setUp() {
    t1 = new Tag("t1");
    t2 = new Tag("t2");
    t3 = new Tag("t3");
  }

  @Test
  public void testGetValue() {
    assertEquals("t1", t1.getValue());
  }

  @Test
  public void testMatches() {
    assertTrue(t1.matches(Matcheable.DefaultPattern.parse("t1")));
  }

  @Test
  public void testCompare() {
    assertTrue(t1.compareTo(t2) < 0);
    assertTrue(t1.compareTo(t1) == 0);
    assertTrue(t2.compareTo(t1) > 0);
  }

  @Test
  public void testAsTags() {
    Set<Tag> actual = Tag.asTags(Collects.arrayToSet("t1", "t2", "t3"));
    assertTrue(actual.containsAll(Collects.arrayToSet(t1, t2, t3)));
  }

  @Test
  public void testSerialization() throws Exception {
    Tag copy = (Tag) Serialization.deserialize(Serialization.serialize(t1));
    assertEquals(t1, copy);
  }

}
