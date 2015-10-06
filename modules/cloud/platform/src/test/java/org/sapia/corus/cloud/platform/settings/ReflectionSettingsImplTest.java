package org.sapia.corus.cloud.platform.settings;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReflectionSettingsImplTest {

  @SuppressWarnings("unused")
  public static class TestSettings {
    private String stringSetting = "stringValue";
    private int    intSetting    = 1;
    private String nullSetting;
  }
  
  private ReflectionSettings settings;
  
  @Before
  public void setUp() throws Exception {
    settings = new ReflectionSettings(new TestSettings());
  }

  @Test
  public void testGet_string() {
    assertEquals("stringValue", settings.get("stringSetting").get(String.class));
  }
  
  @Test
  public void testGet_int() {
    assertEquals(new Integer(1), settings.get("intSetting").get(Integer.class));
  }
  
  @Test
  public void testGetNotNull_from_child() {
    assertEquals("stringValue", settings.getNotNull("stringSetting").get(String.class));
  }
  
  @Test(expected = MissingSettingException.class)
  public void testGetNotNull_with_null() {
    settings.getNotNull("nullSetting");
  }
  
  @Test(expected = MissingSettingException.class)
  public void testGetNotNull_with_unknown_field() {
    settings.getNotNull("test");
  }

  @Test
  public void testSet() {
    settings.set("stringSetting", "test");
    assertEquals("test", settings.get("stringSetting").get(String.class));
  }
}
