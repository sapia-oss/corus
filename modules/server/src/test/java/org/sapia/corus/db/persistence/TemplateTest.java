package org.sapia.corus.db.persistence;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.Record;

public class TemplateTest {

  ClassDescriptor<TestPersistentObject> cd;
  TestPersistentObject tpo;
  
  @Before
  public void setUp() throws Exception {
    cd = new ClassDescriptor<TestPersistentObject>(TestPersistentObject.class);
    tpo = new TestPersistentObject();
    tpo.setId(10);
    tpo.setActive(true);
    tpo.setName("test");
  }

  @Test
  public void testMatchesRecord() {
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setId(10);
    tpo2.setActive(true);
    tpo2.setName("test");
    
    Record<TestPersistentObject> rec = Record.createFor(cd, tpo);
    
    Template<TestPersistentObject> template = new Template<TestPersistentObject>(cd, tpo2);
    template.matches(rec);
  }

  @Test
  public void testNotMatchesRecord() {
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setId(10);
    tpo2.setActive(false);
    tpo2.setName("test");
    
    Record<TestPersistentObject> rec = Record.createFor(cd, tpo);
    
    Template<TestPersistentObject> template = new Template<TestPersistentObject>(cd, tpo2);
    template.matches(rec);
  }
  
  @Test
  public void testMatchesObject() {
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setId(10);
    tpo2.setActive(true);
    tpo2.setName("test");
    
    Template<TestPersistentObject> template = new Template<TestPersistentObject>(cd, tpo2);
    template.matches(tpo);
  }
  
  @Test
  public void testNotMatchesObject() {
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setId(10);
    tpo2.setActive(true);
    tpo2.setName("test2");
    
    Template<TestPersistentObject> template = new Template<TestPersistentObject>(cd, tpo2);
    template.matches(tpo);
  }

}
