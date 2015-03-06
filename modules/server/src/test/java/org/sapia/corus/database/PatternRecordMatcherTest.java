package org.sapia.corus.database;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.PatternRecordMatcher;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.database.HashDbMap;

public class PatternRecordMatcherTest {

  DbMap<String, TestDbObject> map;
  PatternRecordMatcher<TestDbObject> matcher;
  
  @Before
  public void setUp() throws Exception {
    map  = new HashDbMap<String, TestDbObject>(new ClassDescriptor<TestDbObject>(TestDbObject.class));
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
