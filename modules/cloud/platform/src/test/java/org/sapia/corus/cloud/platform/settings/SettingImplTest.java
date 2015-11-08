package org.sapia.corus.cloud.platform.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SettingImplTest {
  
  private SettingImpl valueSetting;
  private SettingImpl nullValueSetting;

  @Before
  public void setUp() throws Exception {
    valueSetting     = new SettingImpl("test", "test-value");
    nullValueSetting = new SettingImpl("test", null);
  }

  @Test
  public void testGetName() {
    assertEquals("test", valueSetting.getName());
  }

  @Test
  public void testIsNull() {
    assertTrue(nullValueSetting.isNull());
  }

  @Test
  public void testIsNull_false() {
    assertFalse(valueSetting.isNull());
  }
  
  @Test
  public void testIsSet() {
    assertTrue(valueSetting.isSet());
  }

  @Test
  public void testIsSet_false() {
    assertFalse(nullValueSetting.isSet());
  }
  
  @Test
  public void testGet() {
    assertEquals("test-value", valueSetting.get(String.class));
  }

  @Test
  public void testGet_with_default() {
    assertEquals("test-value", nullValueSetting.get(String.class, "test-value"));
  }

  @Test
  public void testGetListOf() {
    List<String> list     = Arrays.asList("1", "2");
    List<String> returned = new SettingImpl("test", list).getListOf(String.class);
      
    assertEquals(list.size(), returned.size());
  }

  @Test
  public void testGetSetOf() {
    Set<String> set      = new HashSet<String>(Arrays.asList("1", "2"));
    Set<String> returned = new SettingImpl("test", set).getSetOf(String.class);
      
    assertEquals(set.size(), returned.size());
  }

  @Test
  public void testGetMapOf() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("k", "v");
    Map<String, String> returned = new SettingImpl("test", map).getMapOf(String.class, String.class);
    
    assertEquals(map.size(), returned.size());
  }

}
