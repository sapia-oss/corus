package org.sapia.corus.configurator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguratorImplTest {
  
  private DbMap<String, ConfigProperty> serverPropertyDb;
  private DbMap<String, ConfigProperty> serverProperties;
  
  private DbMap<String, ConfigProperty> processPropertyDb;
  private DbMap<String, ConfigProperty> processProperties;
  
  @Mock
  private DbMap<String, ConfigProperty> tagsProperties;
  
  private DbMap<String, ConfigProperty> internalConfig;
 
  @Mock
  private EventDispatcher dispatcher;
  
  private DbModule db;
  
  private ConfiguratorImpl configurator;
  
  @Before
  public void setUp() {
    
    db = new DbModule() {
      @Override
      public String getRoleName() {
        return DbModule.ROLE;
      }
      
      @Override
      public <K, V> DbMap<K, V> getDbMap(Class<K> keyType, Class<V> valueType,
          String name) {
        return new InMemoryDbMap<>(new ClassDescriptor<>(valueType), new Func<V, JsonInput>() {
          @Override
          public V call(JsonInput in) {
            throw new UnsupportedOperationException();
          }
        });
      }
    };
    
    internalConfig = new InMemoryDbMap<>(new ClassDescriptor<>(ConfigProperty.class), new Func<ConfigProperty, JsonInput>() {
      @Override
      public ConfigProperty call(JsonInput in) {
        throw new UnsupportedOperationException();
      }
    });
    
    serverPropertyDb = new InMemoryDbMap<>(new ClassDescriptor<>(ConfigProperty.class));
    serverProperties = Mockito.spy(serverPropertyDb);
    processPropertyDb = new InMemoryDbMap<>(new ClassDescriptor<>(ConfigProperty.class));
    processProperties = Mockito.spy(processPropertyDb);
        
    configurator = new ConfiguratorImpl();
    configurator.setServerProperties(new PropertyStore(serverProperties));
    configurator.setProcessProperties(new PropertyStore(processProperties));
    configurator.setTags(tagsProperties);
    configurator.setDispatcher(dispatcher);
    configurator.setInternalConfig(internalConfig);
    configurator.setDb(db);
  }

  protected void assertPropertyChangeEvent(EventType eType, PropertyScope eScope, Property[] eProperties, PropertyChangeEvent actual) {
    assertThat(actual.getEventType()).isEqualTo(eType);
    assertThat(actual.getScope()).isEqualTo(eScope);
    assertThat(actual.getProperties()).containsOnly(eProperties);
  }
  
  
  @Test
  public void testAddProcessProperty() {
    configurator.addProperty(PropertyScope.PROCESS, "test", "testValue", new HashSet<String>());
    
    verify(processProperties).put(eq("test"), eq(new ConfigProperty("test", "testValue")));
    verify(dispatcher).dispatch(any(PropertyChangeEvent.class));
    
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent(EventType.ADD, "test", "testValue", PropertyScope.PROCESS)));
  }
  
  @Test
  public void testAddProcessProperty_categories() {
    configurator.addProperty(PropertyScope.PROCESS, "test", "testValue", Collects.arrayToSet("cat1", "cat2"));
    
    assertEquals("testValue", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test"));
    assertEquals("testValue", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test"));

    
    verify(processProperties, never()).put(eq("test"), eq(new ConfigProperty("test", "testValue")));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] { new Property("test", "testValue", "cat1"), new Property("test", "testValue", "cat2") },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, new HashSet<String>(), false);
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(processProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties_clearExisting_nothingToClear() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, new HashSet<String>(), true);
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(processProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties_clearExisting_differentProperties() {
    processPropertyDb.put("test8", new ConfigProperty("test8", "oldValue8"));
    processPropertyDb.put("test9", new ConfigProperty("test9", "oldValue9"));

    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, new HashSet<String>(), true);
    
    verify(processProperties).remove(eq("test8"));
    verify(processProperties).remove(eq("test9"));
    verify(processProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(processProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(2)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(2);
    assertPropertyChangeEvent(EventType.REMOVE, PropertyScope.PROCESS,
        new Property[] { new Property("test8", "oldValue8", null), new Property("test9", "oldValue9", null) },
        captor.getAllValues().get(0));
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getAllValues().get(1));
  }
  
  @Test
  public void testAddProcessProperties_clearExisting_sameProperties() {
    processPropertyDb.put("test1", new ConfigProperty("test1", "oldValue8"));
    processPropertyDb.put("test2", new ConfigProperty("test2", "oldValue9"));

    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, new HashSet<String>(), true);
    
    verify(processProperties).remove(eq("test1"));
    verify(processProperties).remove(eq("test2"));
    verify(processProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(processProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(1)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(1);
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getAllValues().get(0));
  }
  
  @Test
  public void testAddProcessProperties_categories() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, Collects.arrayToSet("cat1", "cat2"), false);

    assertEquals("value1", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test1"));
    assertEquals("value2", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test2"));
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties, never()).put(anyString(), any(ConfigProperty.class));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", "cat1"), new Property("test2", "value2", "cat1"),
            new Property("test1", "value1", "cat2"), new Property("test2", "value2", "cat2") },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties_categories_clearExisting_nothingToClear() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, Collects.arrayToSet("cat1", "cat2"), true);

    assertEquals("value1", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test1"));
    assertEquals("value2", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test2"));
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties, never()).put(anyString(), any(ConfigProperty.class));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", "cat1"), new Property("test2", "value2", "cat1"),
            new Property("test1", "value1", "cat2"), new Property("test2", "value2", "cat2") },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties_categories_clearExisting_differentCategories() {
    configurator.store("cat9", true).addProperty("test9", "value9");
    
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, Collects.arrayToSet("cat1", "cat2"), true);

    assertEquals("value1", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test1"));
    assertEquals("value2", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test2"));
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties, never()).put(anyString(), any(ConfigProperty.class));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", "cat1"), new Property("test2", "value2", "cat1"),
            new Property("test1", "value1", "cat2"), new Property("test2", "value2", "cat2") },
        captor.getValue());
  }
  
  @Test
  public void testAddProcessProperties_categories_clearExisting_sameCategoriesDifferentProps() {
    configurator.store("cat1", true).addProperty("test9", "value9");
    
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, Collects.arrayToSet("cat1", "cat2"), true);

    assertEquals("value1", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test1"));
    assertEquals("value2", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test2"));
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties, never()).put(anyString(), any(ConfigProperty.class));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(2)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(2);
    assertPropertyChangeEvent(EventType.REMOVE, PropertyScope.PROCESS,
        new Property[] { new Property("test9", "value9", "cat1") },
        captor.getAllValues().get(0));
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", "cat1"), new Property("test2", "value2", "cat1"),
            new Property("test1", "value1", "cat2"), new Property("test2", "value2", "cat2") },
        captor.getAllValues().get(1));
  }
  
  @Test
  public void testAddProcessProperties_categories_clearExisting_sameCategoriesSameProps() {
    configurator.store("cat1", true).addProperty("test1", "oldValue1");
    configurator.store("cat1", true).addProperty("test2", "oldValue2");
    configurator.store("cat2", true).addProperty("test1", "oldValue1");
    configurator.store("cat2", true).addProperty("test2", "oldValue2");
    
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, Collects.arrayToSet("cat1", "cat2"), true);

    assertEquals("value1", configurator.getProcessPropertiesByCategory().get("cat1").getProperty("test1"));
    assertEquals("value2", configurator.getProcessPropertiesByCategory().get("cat2").getProperty("test2"));
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties, never()).put(anyString(), any(ConfigProperty.class));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(1)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(1);
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", "cat1"), new Property("test2", "value2", "cat1"),
            new Property("test1", "value1", "cat2"), new Property("test2", "value2", "cat2") },
        captor.getAllValues().get(0));
  }

  @Test
  public void testAddServerProperty() {
    configurator.addProperty(PropertyScope.SERVER, "test", "testValue", new HashSet<String>());
    
    verify(serverProperties).put(eq("test"), eq(new ConfigProperty("test", "testValue")));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent(EventType.ADD, "test", "testValue", PropertyScope.SERVER)));
  }
  
  @Test
  public void testAddServerProperties() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.SERVER, props, new HashSet<String>(), false);
    
    verify(serverProperties, never()).remove(any(String.class));
    verify(serverProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(serverProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.SERVER,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testAddServerProperties_clearExisting_nothingToClear() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.SERVER, props, new HashSet<String>(), true);
    
    verify(serverProperties, never()).remove(any(String.class));
    verify(serverProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(serverProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.SERVER,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testAddServerProperties_clearExisting_differentProperties() {
    serverPropertyDb.put("test9", new ConfigProperty("test9", "value9"));
    
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.SERVER, props, new HashSet<String>(), true);
    
    verify(serverProperties).remove(eq("test9"));
    verify(serverProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(serverProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(2)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(2);
    assertPropertyChangeEvent(EventType.REMOVE, PropertyScope.SERVER,
        new Property[] { new Property("test9", "value9", null) },
        captor.getAllValues().get(0));
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.SERVER,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getAllValues().get(1));
  }
  
  @Test
  public void testAddServerProperties_clearExisting_sameProperties() {
    serverPropertyDb.put("test1", new ConfigProperty("test1", "oldValue1"));
    serverPropertyDb.put("test2", new ConfigProperty("test2", "oldValue2"));
    
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.SERVER, props, new HashSet<String>(), true);
    
    verify(serverProperties).remove(eq("test1"));
    verify(serverProperties).remove(eq("test2"));
    verify(serverProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(serverProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher, times(1)).dispatch(captor.capture());

    assertThat(captor.getAllValues()).hasSize(1);
    assertPropertyChangeEvent(EventType.ADD, PropertyScope.SERVER,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getAllValues().get(0));
  }
  
  @Test
  public void testGetProcessProperty() {
    when(processProperties.get(anyString())).thenReturn(new ConfigProperty("test", "value"));
    
    configurator.getProperty("test", new ArrayList<String>());
    
    verify(processProperties).get(eq("test"));
  }
  
  @Test
  public void testGetServerProperty() {
    when(serverProperties.get(anyString())).thenReturn(new ConfigProperty("test", "value"));
    
    configurator.getProperty("test", new ArrayList<String>());
    
    verify(serverProperties).get(eq("test"));
  }

  @Test
  public void testRemoveProcessProperty() {
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));
    
    configurator.removeProperty(PropertyScope.PROCESS, ArgMatchers.any(), new HashSet<ArgMatcher>());
    
    verify(processProperties).remove(eq("test1"));
    verify(processProperties).remove(eq("test2"));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.REMOVE, PropertyScope.PROCESS, 
        new Property[] {
            new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testRemoveProcessProperty_categories() {
    configurator.store("cat1", true).addProperty("test", "value");
    configurator.store("cat2", true).addProperty("test", "value");

    assertEquals("value", configurator.store("cat2", false).getProperty("test"));
    configurator.removeProperty(PropertyScope.PROCESS, ArgMatchers.any(), Collects.arrayToSet(ArgMatchers.parse("cat1")));
    assertEquals("value", configurator.store("cat2", false).getProperty("test"));

    verify(processProperties, never()).remove(eq("test"));
    assertNull(configurator.store("cat1", false).getProperty("test"));
    assertEquals("value", configurator.store("cat2", false).getProperty("test"));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent(EventType.REMOVE, "test", "value", "cat1", PropertyScope.PROCESS)));
    verify(dispatcher, never()).dispatch(eq(new PropertyChangeEvent(EventType.REMOVE, "test", "value", "cat2", PropertyScope.PROCESS)));
  }

  @Test
  public void testRemoveServerProperty() {
    when(serverProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));
    
    configurator.removeProperty(PropertyScope.SERVER, ArgMatchers.any(), new HashSet<ArgMatcher>());
    
    verify(serverProperties).remove(eq("test1"));
    verify(serverProperties).remove(eq("test2"));
    ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
    verify(dispatcher).dispatch(captor.capture());
    
    assertPropertyChangeEvent(EventType.REMOVE, PropertyScope.SERVER,
        new Property[] { new Property("test1", "value1", null), new Property("test2", "value2", null) },
        captor.getValue());
  }
  
  @Test
  public void testGetProcessProperties() {
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    Properties props = configurator.getProperties(PropertyScope.PROCESS, new ArrayList<String>());

    assertEquals("value1", props.getProperty("test1"));
    assertEquals("value2", props.getProperty("test2"));
  }
  
  @Test
  public void testGetProcessProperties_categories_first_category_match() {
    configurator.store("cat1", true).addProperty("test", "value1");
    configurator.store("cat2", true).addProperty("test2", "value2");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    Properties props = configurator.getProperties(PropertyScope.PROCESS, Collects.arrayToList("cat1", "cat2"));

    assertEquals("value1", props.getProperty("test"));
  }
  
  @Test
  public void testGetProcessProperties_categories_last_category_match() {
    configurator.store("cat1", true).addProperty("test1", "value1");
    configurator.store("cat2", true).addProperty("test", "value2");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    Properties props = configurator.getProperties(PropertyScope.PROCESS, Collects.arrayToList("cat1", "cat2"));

    assertEquals("value2", props.getProperty("test"));
  }
  
  @Test
  public void testGetProcessProperties_categories_no_category_match() {
    configurator.store("cat1", true).addProperty("test1", "value1");
    configurator.store("cat2", true).addProperty("test2", "value2");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    Properties props = configurator.getProperties(PropertyScope.PROCESS, Collects.arrayToList("cat1", "cat2"));

    assertEquals("value", props.getProperty("test"));
  }

  @Test
  public void testGetServerProperties() {
    when(serverProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    Properties props = configurator.getProperties(PropertyScope.SERVER, new ArrayList<String>());

    assertEquals("value1", props.getProperty("test1"));
    assertEquals("value2", props.getProperty("test2"));
  }
  
  @Test
  public void testGetProcessPropertyList() {
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<Property> props = configurator.getPropertiesList(PropertyScope.PROCESS, new ArrayList<String>());
    
    assertTrue(props.contains(new Property("test1", "value1", null)));
    assertTrue(props.contains(new Property("test2", "value2", null)));
  }
  
  @Test
  public void testGetProcessPropertyList_categories_all() {
    
    configurator.store("cat1", true).addProperty("test1", "value1");
    configurator.store("cat2", true).addProperty("test2", "value2");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    List<Property> props = configurator.getPropertiesList(PropertyScope.PROCESS, Collects.arrayToList("cat1", "cat2"));
    
    assertTrue(props.contains(new Property("test", "value", null)));
    assertTrue(props.contains(new Property("test1", "value1", "cat1")));
    assertTrue(props.contains(new Property("test2", "value2", "cat2")));
  }
  
  @Test
  public void testGetProcessPropertyList_categories_no_match() {
    
    configurator.store("cat1", true).addProperty("test1", "value1");
    configurator.store("cat2", true).addProperty("test2", "value2");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    List<Property> props = configurator.getPropertiesList(PropertyScope.PROCESS, Collects.arrayToList("cat3", "cat4"));
    
    assertTrue(props.contains(new Property("test", "value", null)));
    assertFalse(props.contains(new Property("test1", "value1", "cat1")));
    assertFalse(props.contains(new Property("test2", "value2", "cat2")));
  }
  
  @Test
  public void testGetProcessPropertyList_categories_partial_match() {
    
    configurator.store("cat1", true).addProperty("test1", "value1");
    configurator.store("cat2", true).addProperty("test", "value3");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    when(processProperties.get("test")).thenReturn(new ConfigProperty("test", "value"));

    List<Property> props = configurator.getPropertiesList(PropertyScope.PROCESS, Collects.arrayToList("cat1", "cat2"));
    
    assertFalse(props.contains(new Property("test", "value", null)));
    assertTrue(props.contains(new Property("test1", "value1", "cat1")));
    assertTrue(props.contains(new Property("test", "value3", "cat2")));
  }
  
  @Test
  public void testGetProcessAllPropertyList() {
    
    configurator.store("cat1", true).addProperty("test3", "value3");
    configurator.store("cat2", true).addProperty("test4", "value4");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<Property> props = configurator.getAllPropertiesList(PropertyScope.PROCESS, new HashSet<ArgMatcher>());
    
    assertTrue(props.contains(new Property("test1", "value1", null)));
    assertTrue(props.contains(new Property("test2", "value2", null)));
    assertTrue(props.contains(new Property("test3", "value3", "cat1")));
    assertTrue(props.contains(new Property("test4", "value4", "cat2")));

  }
  
  @Test
  public void testGetProcessAllPropertyList_for_categories() {
    
    configurator.store("cat1", true).addProperty("test3", "value3");
    configurator.store("cat2", true).addProperty("test4", "value4");
    
    when(processProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<Property> props = configurator.getAllPropertiesList(PropertyScope.PROCESS, Collects.arrayToSet(ArgMatchers.parse("cat2")));
    
    assertFalse(props.contains(new Property("test1", "value1", null)));
    assertFalse(props.contains(new Property("test2", "value2", null)));
    assertFalse(props.contains(new Property("test3", "value3", "cat1")));
    assertTrue(props.contains(new Property("test4", "value4", "cat2")));

  }
  
  @Test
  public void testGetServerPropertyList() {
    when(serverProperties.keys()).thenReturn(Collects.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<Property> props = configurator.getPropertiesList(PropertyScope.SERVER, new ArrayList<String>());
    
    assertTrue(props.contains(new Property("test1", "value1", null)));
    assertTrue(props.contains(new Property("test2", "value2", null)));
  }

  @Test
  public void testAddTag() {
    configurator.addTag("test");
    verify(tagsProperties).put("test", new ConfigProperty("test", "test"));
  }

  @Test
  public void testClearTags() {
    configurator.clearTags();
    verify(tagsProperties).clear();
  }

  @Test
  public void testGetTags() {
    when(tagsProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());

    Set<Tag> tags = configurator.getTags();
    
    assertTrue(tags.contains(new Tag("test")));
  }

  @Test
  public void testRemoveTagString() {
    configurator.removeTag("test");
    verify(tagsProperties).remove("test");
  }

  @Test
  public void testRemoveTagArg() {
    when(tagsProperties.keys()).thenReturn(Collects.arrayToList("test").iterator());
    configurator.removeTag(ArgMatchers.any());
    verify(tagsProperties).remove("test");
  }

  @Test
  public void testAddTags() {
    configurator.addTags(Collects.arrayToSet("test1", "test2"), false);
    verify(tagsProperties).put("test1", new ConfigProperty("test1", "test1"));
    verify(tagsProperties).put("test2", new ConfigProperty("test2", "test2"));
  }
  
  @Test
  public void testAddTags_clear_existing() {
    configurator.addTags(Collects.arrayToSet("test1", "test2"), false);
    configurator.addTags(Collects.arrayToSet("test3", "test4"), true);

    verify(tagsProperties).put("test1", new ConfigProperty("test1", "test1"));
    verify(tagsProperties).put("test2", new ConfigProperty("test2", "test2"));
    verify(tagsProperties).put("test3", new ConfigProperty("test3", "test3"));
    verify(tagsProperties).put("test4", new ConfigProperty("test4", "test4"));
    verify(tagsProperties).clear();
  }
  
  @Test
  public void testRenameTags() {
    when(tagsProperties.get("test1")).thenReturn(new ConfigProperty("test1", "test1"));
    configurator.addTags(Collects.arrayToSet("test1"), false);
    configurator.renameTags(Collects.arrayToList(new NameValuePair("test1", "test2")));
    verify(tagsProperties).put("test1", new ConfigProperty("test1", "test1"));
    verify(tagsProperties).remove("test1");
    verify(tagsProperties).put("test2", new ConfigProperty("test2", "test2"));
  }

}
