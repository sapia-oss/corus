package org.sapia.corus.client.services.security;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;

public class AppKeyConfigTest {

  private AppKeyConfig conf;
  
  @Before
  public void setUp() {
    conf = new AppKeyConfig("app", "test", "1234");
  }
  
  @Test
  public void testJson() {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    conf.toJson(stream, ContentLevel.DETAIL);
    JsonInput in = JsonObjectInput.newInstance(writer.toString());
    AppKeyConfig copy = AppKeyConfig.fromJson(in);
    
    assertEquals(conf.getAppId(), copy.getAppId());
    assertEquals(conf.getApplicationKey(), copy.getApplicationKey());
    assertEquals(conf.getKey(), copy.getKey());
    assertEquals(conf.getRole(), copy.getRole());
  }
  
  @Test
  public void testSerialization() {
    AppKeyConfig copy = (AppKeyConfig) SerializationUtils.deserialize(SerializationUtils.serialize(conf));
    
    assertEquals(conf.getAppId(), copy.getAppId());
    assertEquals(conf.getApplicationKey(), copy.getApplicationKey());
    assertEquals(conf.getKey(), copy.getKey());
    assertEquals(conf.getRole(), copy.getRole());
  }
}
