package org.sapia.corus.configurator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.ubik.util.Collections2;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguratorImplTest {
  
  @Mock
  private DbMap<String, ConfigProperty> serverProperties;
  
  @Mock
  private DbMap<String, ConfigProperty> processProperties;
  
  @Mock
  private DbMap<String, ConfigProperty> tagsProperties;
  
  @Mock
  private EventDispatcher dispatcher;
  
  private ConfiguratorImpl configurator;
  
  @Before
  public void setUp() {
    configurator = new ConfiguratorImpl();
    configurator.setServerProperties(new PropertyStore(serverProperties));
    configurator.setProcessProperties(new PropertyStore(processProperties));
    configurator.setTags(tagsProperties);
    configurator.setDispatcher(dispatcher);
  }

  @Test
  public void testAddProcessProperty() {
    configurator.addProperty(PropertyScope.PROCESS, "test", "testValue");
    
    verify(processProperties).put(eq("test"), eq(new ConfigProperty("test", "testValue")));
    verify(dispatcher).dispatch(any(PropertyChangeEvent.class));
    
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test", "testValue", PropertyScope.PROCESS, Type.ADD)));
  }
  
  @Test
  public void testAddProcessProperties() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.PROCESS, props, false);
    
    verify(processProperties, never()).remove(any(String.class));
    verify(processProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(processProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test1", "value1", PropertyScope.PROCESS, Type.ADD)));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test2", "value2", PropertyScope.PROCESS, Type.ADD)));

  }

  @Test
  public void testAddServerProperty() {
    configurator.addProperty(PropertyScope.SERVER, "test", "testValue");
    
    verify(serverProperties).put(eq("test"), eq(new ConfigProperty("test", "testValue")));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test", "testValue", PropertyScope.SERVER, Type.ADD)));
  }
  
  @Test
  public void testAddServerProperties() {
    Properties props = new Properties();
    props.setProperty("test1", "value1");
    props.setProperty("test2", "value2");
    
    configurator.addProperties(PropertyScope.SERVER, props, false);
    
    verify(serverProperties, never()).remove(any(String.class));
    verify(serverProperties).put(eq("test1"), eq(new ConfigProperty("test1", "value1")));
    verify(serverProperties).put(eq("test2"), eq(new ConfigProperty("test2", "value2")));
    
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test1", "value1", PropertyScope.SERVER, Type.ADD)));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test2", "value2", PropertyScope.SERVER, Type.ADD)));

  }
  
  @Test
  public void testGetProcessProperty() {
    when(processProperties.get(anyString())).thenReturn(new ConfigProperty("test", "value"));
    
    configurator.getProperty("test");
    
    verify(processProperties).get(eq("test"));
  }
  
  @Test
  public void testGetServerProperty() {
    when(serverProperties.get(anyString())).thenReturn(new ConfigProperty("test", "value"));
    
    configurator.getProperty("test");
    
    verify(serverProperties).get(eq("test"));
  }

  @Test
  public void testRemoveProcessProperty() {
    when(processProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));
    
    configurator.removeProperty(PropertyScope.PROCESS, ArgFactory.any());
    
    verify(processProperties).remove(eq("test1"));
    verify(processProperties).remove(eq("test2"));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test1", "value1", PropertyScope.PROCESS, Type.REMOVE)));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test2", "value2", PropertyScope.PROCESS, Type.REMOVE)));

  }

  @Test
  public void testRemoveServerProperty() {
    when(serverProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));
    
    configurator.removeProperty(PropertyScope.SERVER, ArgFactory.any());
    
    verify(serverProperties).remove(eq("test1"));
    verify(serverProperties).remove(eq("test2"));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test1", "value1", PropertyScope.SERVER, Type.REMOVE)));
    verify(dispatcher).dispatch(eq(new PropertyChangeEvent("test2", "value2", PropertyScope.SERVER, Type.REMOVE)));
  }
  
  @Test
  public void testGetProcessProperties() {
    when(processProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    Properties props = configurator.getProperties(PropertyScope.PROCESS);

    assertEquals("value1", props.getProperty("test1"));
    assertEquals("value2", props.getProperty("test2"));
  }

  @Test
  public void testGetServerProperties() {
    when(serverProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    Properties props = configurator.getProperties(PropertyScope.SERVER);

    assertEquals("value1", props.getProperty("test1"));
    assertEquals("value2", props.getProperty("test2"));
  }
  
  @Test
  public void testGetProcessPropertiesAsNameValuePairs() {
    when(processProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(processProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(processProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<NameValuePair> props = configurator.getPropertiesAsNameValuePairs(PropertyScope.PROCESS);
    
    assertTrue(props.contains(new NameValuePair("test1", "value1")));
    assertTrue(props.contains(new NameValuePair("test2", "value2")));
  }
  
  @Test
  public void testGetServerPropertiesAsNameValuePairs() {
    when(serverProperties.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    when(serverProperties.get("test1")).thenReturn(new ConfigProperty("test1", "value1"));
    when(serverProperties.get("test2")).thenReturn(new ConfigProperty("test2", "value2"));

    List<NameValuePair> props = configurator.getPropertiesAsNameValuePairs(PropertyScope.SERVER);
    
    assertTrue(props.contains(new NameValuePair("test1", "value1")));
    assertTrue(props.contains(new NameValuePair("test2", "value2")));
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
    when(tagsProperties.keys()).thenReturn(Collections2.arrayToList("test").iterator());

    Set<String> tags = configurator.getTags();
    
    assertTrue(tags.contains("test"));
  }

  @Test
  public void testRemoveTagString() {
    configurator.removeTag("test");
    verify(tagsProperties).remove("test");
  }

  @Test
  public void testRemoveTagArg() {
    when(tagsProperties.keys()).thenReturn(Collections2.arrayToList("test").iterator());
    configurator.removeTag(ArgFactory.any());
    verify(tagsProperties).remove("test");
  }

  @Test
  public void testAddTags() {
    configurator.addTags(Collections2.arrayToSet("test1", "test2"));
    verify(tagsProperties).put("test1", new ConfigProperty("test1", "test1"));
    verify(tagsProperties).put("test2", new ConfigProperty("test2", "test2"));
  }
  
  @Test
  public void testReplaceTags() {
    when(tagsProperties.get("test1")).thenReturn(new ConfigProperty("test1", "test1"));
    configurator.addTags(Collections2.arrayToSet("test1"));
    configurator.renameTags(Collections2.arrayToList(new NameValuePair("test1", "test2")));
    verify(tagsProperties).put("test1", new ConfigProperty("test1", "test1"));
    verify(tagsProperties).remove("test1");
    verify(tagsProperties).put("test2", new ConfigProperty("test2", "test2"));
  }

}
