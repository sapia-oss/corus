package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;
import org.junit.Test;

public class InterpolationTest {

  @Test
  public void testInterpolate_properties() {
    Map<String, String> vars = new HashMap<>();
    vars.put("key1", "value1");
    
    Properties props = new Properties();
    props.setProperty("test1", "${test2}");
    props.setProperty("test2", "${key1}");
    
    props = Interpolation.interpolate(props, StrLookup.mapLookup(vars), 2);
    
    assertEquals("value1", props.getProperty("test1"));
  }

  @Test
  public void testInterpolate_map() {
    Map<String, String> vars = new HashMap<>();
    vars.put("key1", "value1");
    vars.put("key2",  "value2");
    
    Map<String, String> props = new HashMap<>();
    props.put("test1", "${test2}");
    props.put("test2", "${key1}");
    
    props = Interpolation.interpolate(props, StrLookup.mapLookup(vars), 2);
    
    assertEquals("value1", props.get("test1"));
  }

}
