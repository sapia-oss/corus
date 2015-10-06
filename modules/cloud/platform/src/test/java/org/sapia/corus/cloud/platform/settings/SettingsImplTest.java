package org.sapia.corus.cloud.platform.settings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SettingsImplTest {
  
  @Mock
  private SettingsImpl parent;
  private SettingsImpl child;
  private SettingsImpl orphan;
  
  private Map<String, Object> settingValues;

  @Before
  public void setUp() throws Exception {
    when(parent.get("s2")).thenReturn(new SettingImpl("s2", "v2"));
    settingValues = new HashMap<String, Object>();
    settingValues.put("s1", "v1");
    
    this.child  = new SettingsImpl(parent, settingValues);
    this.orphan = new SettingsImpl(settingValues);
  }

  @Test
  public void testGet_from_child() {
    assertEquals("v1", child.get("s1").get(String.class));
  }
  
  @Test
  public void testGet_from_parent() {
    assertEquals("v2", child.get("s2").get(String.class));
  }
  
  @Test
  public void testGet_null_from_orhan() {
    assertTrue("v2", orphan.get("s2").isNull());
  }
  
  @Test
  public void testGetNotNull_from_child() {
    assertEquals("v1", child.getNotNull("s1").get(String.class));
  }
  
  @Test
  public void testGetNotNull_from_parent() {
    assertEquals("v2", child.getNotNull("s2").get(String.class));
  }
  
  @Test(expected = MissingSettingException.class)
  public void testGetNotNull_with_null() {
    orphan.getNotNull("s2");
  }

  @Test
  public void testSet_child() {
    child.set("s3", "v3");
    assertEquals("v3", child.get("s3").get(String.class));
  }

  @Test
  public void testSet_orphan() {
    orphan.set("s3", "v3");
    assertEquals("v3", orphan.get("s3").get(String.class));
  }
}
