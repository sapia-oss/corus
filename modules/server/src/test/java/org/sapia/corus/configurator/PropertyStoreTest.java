package org.sapia.corus.configurator;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.database.InMemoryArchiver;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

public class PropertyStoreTest {
  
  PropertyStore store;

  @Before
  public void setUp() throws Exception {
    store = new PropertyStore(new InMemoryDbMap<String, ConfigProperty>(new ClassDescriptor<ConfigProperty>(ConfigProperty.class), 
      new InMemoryArchiver<String, ConfigProperty>(),
      new Func<ConfigProperty, JsonInput>() {
        @Override
        public ConfigProperty call(JsonInput in) {
          throw new UnsupportedOperationException();
        }
    }));
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
    store.removeProperty(ArgMatchers.parse("test*"));
    assertEquals(null, store.getProperty("test1"));
    assertEquals(null, store.getProperty("test2"));
    assertEquals("value", store.getProperty("foo"));
  }

  @Test
  public void testGetProperties() {
    store.addProperty("test1", "value1");
    store.addProperty("test2", "value2");
    Properties props = store.getProperties();
    assertEquals("value1", props.getProperty("test1"));
    assertEquals("value2", props.getProperty("test2"));
  }
  
  @Test
  public void testArchive() {
    store.addProperty("test1", "value1");
    store.addProperty("test2", "value2");
    
    store.archive(RevId.valueOf("rev"));
    store.removeProperty(ArgMatchers.parse("*"));
    assertEquals(0, store.getProperties().size());
    
    store.unarchive(RevId.valueOf("rev"));
    assertEquals(2, store.getProperties().size());
  }

  
  @Test
  public void testArchive_clear_previous_rev() {
    store.addProperty("test1", "value1");
    store.archive(RevId.valueOf("rev"));
    store.removeProperty(ArgMatchers.parse("*"));
    
    store.addProperty("test2", "value2");
    store.archive(RevId.valueOf("rev"));
    store.unarchive(RevId.valueOf("rev"));
    
    assertEquals(1, store.getProperties().size());
    assertEquals("value2", store.getProperty("test2"));

  }
}
