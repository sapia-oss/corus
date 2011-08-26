package org.sapia.corus.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;

public class HashDbMapTest {
  
  DbMap<String, TestDbObject> map;
  
  @Before
  public void setUp() throws Exception {
    map  = new HashDbMap<String, TestDbObject>(new ClassDescriptor<TestDbObject>(TestDbObject.class));
    for(int i = 0; i < 5; i++){
      TestDbObject dbo = new TestDbObject();
      dbo.setName("name"+i);
      dbo.setDescription("desc"+i);
      map.put(dbo.getName(), dbo);
    }
  }

  @Test
  public void testGet() {
    TestDbObject dbo = map.get("name0");
    assertTrue("Object not found", dbo != null);
  }

  @Test
  public void testKeys() {
    Iterator<String> keys = map.keys();
    Set<String> toCheck = new HashSet<String>();
    while(keys.hasNext()){
      toCheck.add(keys.next());
    }
    for(int i = 0; i < 5; i++){
      String name = "name"+i;
      assertTrue(toCheck.contains(name));
    }
  }

  @Test
  public void testRemove() {
    for(int i = 0; i < 5; i++){
      String name = "name"+i;
      map.remove(name);
      assertTrue("Object not removed " + name, map.get(name) == null);
    }
  }

  @Test
  public void testValues() {
    Iterator<TestDbObject> values = map.values();
    Map<String,TestDbObject> toCheck = new HashMap<String,TestDbObject>();
    while(values.hasNext()){
      TestDbObject dbo = values.next();
      toCheck.put(dbo.getName(), dbo);
    }
    
    for(int i = 0; i < 5; i++){
      String name = "name"+i;
      TestDbObject dbo = toCheck.get(name);
      assertTrue("Object not found", dbo != null);
    }

  }

  @Test
  public void testValuesMatcher() {
    TestDbObject tbo = new TestDbObject();
    tbo.setName("name0");
    RecordMatcher<TestDbObject> matcher = map.createMatcherFor(tbo);
    Collection<TestDbObject> result = map.values(matcher);
    assertEquals(1, result.size());
    TestDbObject tboResult = result.iterator().next();
    assertEquals("name0", tboResult.getName());
  }

  @Test
  public void testClear() {
    map.clear();
    assertTrue("Map not cleared", !map.values().hasNext());
  }

}
