package org.sapia.corus.database.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;

public class RecordTest {

  private ClassDescriptor<TestPersistentObject> cd;
  private TestPersistentObject tpo;
  
  @Before
  public void setUp() throws Exception {
    cd = new ClassDescriptor<TestPersistentObject>(TestPersistentObject.class);
    tpo = new TestPersistentObject();
    tpo.setId(10);
    tpo.setName("test");
  }

  @Test
  public void testGetValueAt() {
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    assertEquals(10L, r.getValueAt(cd.getFieldForName("id").getIndex()));
    assertEquals("test", r.getValueAt(cd.getFieldForName("name").getIndex()));
  }
  
  @Test
  public void testGetNullValueAt() {
    tpo.setName(null);
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    assertEquals(null, r.getValueAt(cd.getFieldForName("name").getIndex()));
  }

  @Test
  public void testUpdateWith() {
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setId(100);
    tpo2.setName("test2");
    r.updateWith(cd, tpo2);
    assertEquals(100L, r.getValueAt(cd.getFieldForName("id").getIndex()));
    assertEquals("test2", r.getValueAt(cd.getFieldForName("name").getIndex()));
  }
  
  @Test
  public void testUpdateWithNull() {
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    TestPersistentObject tpo2 = new TestPersistentObject();
    tpo2.setName(null);
    r.updateWith(cd, tpo2);
    assertEquals(null, r.getValueAt(cd.getFieldForName("name").getIndex()));
  }

  @Test
  public void testToObject() {
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    TestPersistentObject tpo2 = r.toObject(cd);
    assertEquals(tpo.getName(), tpo2.getName());
    assertEquals(tpo.getId(), tpo2.getId());
  }
  
  @Test
  public void testPopulate(){
    Record<TestPersistentObject> r = Record.createFor(cd, tpo);
    TestPersistentObject tpo2 = new TestPersistentObject();
    r.populate(cd, tpo2);
    assertEquals(tpo2.getName(), tpo.getName());
    assertEquals(tpo2.getId(), tpo.getId());
    
  }

}
