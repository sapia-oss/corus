package org.sapia.corus.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RecordMatcher;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.ubik.util.Func;

public class InMemoryDbMapTest {
  
  DbMap<String, TestDbObject> map, mapCopy;
  
  @Before
  public void setUp() throws Exception {
    map  = new InMemoryDbMap<String, TestDbObject>(
        new ClassDescriptor<TestDbObject>(TestDbObject.class),
        new InMemoryArchiver<String, TestDbObject>(),
        new Func<TestDbObject, JsonInput>() {
          public TestDbObject call(JsonInput in) {
            return TestDbObject.fromJson(in);
          }
        }
    );
    
    mapCopy  = new InMemoryDbMap<String, TestDbObject>(
        new ClassDescriptor<TestDbObject>(TestDbObject.class),
        new InMemoryArchiver<String, TestDbObject>(),
        new Func<TestDbObject, JsonInput>() {
          public TestDbObject call(JsonInput in) {
            return TestDbObject.fromJson(in);
          }
        }
    );
    
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
  
  @Test
  public void testArchive() {
    TestDbObject tbo = new TestDbObject();
    tbo.setName("name0");
    tbo.setDescription("newVersion0");
    map.archive(RevId.valueOf("rev"), "name0");
    map.put("name0", tbo);
    map.unarchive(RevId.valueOf("rev"));
    
    assertNotEquals("newVersion0", map.get("name0").getDescription());
  }

  @Test 
  public void testArchive_many() {
    TestDbObject tbo1 = new TestDbObject();
    tbo1.setName("name0");
    tbo1.setDescription("newVersion0");
    
    TestDbObject tbo2 = new TestDbObject();
    tbo2.setName("name1");
    tbo2.setDescription("newVersion1");
    
    map.archive(RevId.valueOf("rev"), "name0");
    map.archive(RevId.valueOf("rev"), "name1");
    
    map.put("name0", tbo1);
    map.put("name1", tbo2);
    
    map.unarchive(RevId.valueOf("rev"));
    
    assertNotEquals("newVersion0", map.get("name0").getDescription());
    assertNotEquals("newVersion1", map.get("name1").getDescription());
  }
  
  @Test
  public void testDumpLoad() {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    
    stream.beginObject();
    map.dump(stream);
    map.archive(RevId.valueOf("rev"), "name0");

    stream.endObject();
    
    mapCopy.load(JsonObjectInput.newInstance(writer.toString()));
    
    Iterator<String> keys = map.keys();
    while (keys.hasNext()) {
      String       key      = keys.next();
      TestDbObject original = map.get(key);
      TestDbObject copy     = mapCopy.get(key);
      assertEquals(original.getName(), copy.getName());
      assertEquals(original.getDescription(), copy.getDescription());
    }
    
    TestDbObject tbo1 = new TestDbObject();
    tbo1.setName("name0");
    tbo1.setDescription("newVersion0");
    
    mapCopy.put("name0", tbo1);
    mapCopy.unarchive(RevId.valueOf("rev"));
    assertNotEquals("newVersion0", map.get("name0").getDescription());
  }
}
