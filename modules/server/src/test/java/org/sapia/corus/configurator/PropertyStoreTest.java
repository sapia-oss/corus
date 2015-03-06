package org.sapia.corus.configurator;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.database.HashDbMap;

public class PropertyStoreTest {
  
  PropertyStore store;

  @Before
  public void setUp() throws Exception {
    store = new PropertyStore(new HashDbMap<String, ConfigProperty>(new ClassDescriptor<ConfigProperty>(ConfigProperty.class)));
  }

  @Test
  public void testAddProperty() {
    store.addProperty("test", "value");
    assertEquals("value", store.getProperty("test"));
  }

  @Test
  public void testRemoveProperty() {
    store.addProperty("test", "value");
    store.removeProperty("test");
    assertEquals(null, store.getProperty("test"));
  }

  @Test
  public void testRemovePropertyForArg() {
    store.addProperty("test1", "value1");
    store.addProperty("test2", "value2");
    store.addProperty("foo", "value");
    store.removeProperty(ArgFactory.parse("test*"));
    assertEquals(null, store.getProperty("test1"));
    assertEquals(null, store.getProperty("test2"));
    assertEquals("value", store.getProperty("foo"));
  }

  @Test
  public void testGetProperties() {
    store.addProperty("test1", "value1");
    store.addProperty("test2", "value2");
    Properties props = store.getProperties();
    assertEquals("value1", store.getProperty("test1"));
    assertEquals("value2", store.getProperty("test2"));

    
  }

}
