package org.sapia.corus.database;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.PatternRecordMatcher;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

public class PatternRecordMatcherTest {

  DbMap<String, TestDbObject> map;
  PatternRecordMatcher<TestDbObject> matcher;
  
  @Before
  public void setUp() throws Exception {
    map  = new InMemoryDbMap<String, TestDbObject>(new ClassDescriptor<TestDbObject>(TestDbObject.class), new Func<TestDbObject, JsonInput>() {
      public TestDbObject call(JsonInput arg0) {
        throw new UnsupportedOperationException();
      }
    });
    for(int i = 0; i < 5; i++){
      TestDbObject dbo = new TestDbObject();
      dbo.setName("name"+i);
      dbo.setDescription("desc"+i);
      map.put(dbo.getName(), dbo);
    }
    matcher = PatternRecordMatcher.createFor(map);
  }

  @Test
  public void testAdd() {
    
  }

  @Test
  public void testMatchPatternAny(){
    matcher.addPattern("name", "*").addPattern("description", "*");
    assertEquals(5, map.values(matcher).size());
  }


  @Test
  public void testMatchPatternNull(){
    matcher.addPattern("name", null).addPattern("description", null);
    assertEquals(5, map.values(matcher).size());
  }
  @Test
  public void testMatchPatternSpecific(){
    matcher.addPattern("name", "name0").addPattern("description", "desc0");
    assertEquals(1, map.values(matcher).size());
  }

}
