package org.sapia.corus.database.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.FieldDescriptor;

public class FieldDescriptorTest {

  private ClassDescriptor<TestPersistentObject> cd;

  @Before
  public void setUp() throws Exception {
    cd = new ClassDescriptor<TestPersistentObject>(TestPersistentObject.class);
  }

  @Test
  public void testGetIndex() {

    FieldDescriptor activeField = cd.getFieldForName("active");
    assertEquals(0, activeField.getIndex());

    FieldDescriptor idField = cd.getFieldForName("id");
    assertEquals(1, idField.getIndex());

    FieldDescriptor nameField = cd.getFieldForName("name");
    assertEquals(2, nameField.getIndex());
  }

  @Test
  public void testInvokeAccessor() {
    TestPersistentObject tpo = new TestPersistentObject();
    tpo.setId(10);
    tpo.setName("test");
    tpo.setActive(true);

    FieldDescriptor activeField = cd.getFieldForName("active");
    assertEquals(true, activeField.invokeAccessor(tpo));

    FieldDescriptor idField = cd.getFieldForName("id");
    assertEquals(10L, idField.invokeAccessor(tpo));

    FieldDescriptor nameField = cd.getFieldForName("name");
    assertEquals("test", nameField.invokeAccessor(tpo));

  }

  @Test
  public void testInvokeMutator() {
    TestPersistentObject tpo = new TestPersistentObject();
    tpo.setId(10);
    tpo.setName("test");

    FieldDescriptor activeField = cd.getFieldForName("active");
    activeField.invokeMutator(tpo, false);
    assertEquals(false, tpo.isActive());

    FieldDescriptor idField = cd.getFieldForName("id");
    idField.invokeMutator(tpo, 100L);
    assertEquals(100L, tpo.getId());

    FieldDescriptor nameField = cd.getFieldForName("name");
    nameField.invokeMutator(tpo, "test2");
    assertEquals("test2", tpo.getName());
  }

}
