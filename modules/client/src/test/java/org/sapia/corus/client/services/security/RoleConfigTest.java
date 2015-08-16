package org.sapia.corus.client.services.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.ubik.util.Collects;

public class RoleConfigTest {

  private RoleConfig conf;
  
  @Before
  public void setUp() {
    conf = new RoleConfig("test", Collects.arrayToSet(Permission.values()));
  }
  
  @Test
  public void testJson() {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    conf.toJson(stream, ContentLevel.DETAIL);
    JsonInput in = JsonObjectInput.newInstance(writer.toString());
    RoleConfig copy = RoleConfig.fromJson(in);
    
    assertEquals(conf.getRole(), copy.getRole());
    assertTrue(conf.getPermissions().containsAll(copy.getPermissions()));
  }
  
  @Test
  public void testSerialization() {
    RoleConfig copy = (RoleConfig) SerializationUtils.deserialize(SerializationUtils.serialize(conf));
    
    assertEquals(conf.getRole(), copy.getRole());
    assertTrue(conf.getPermissions().containsAll(copy.getPermissions()));
  }
}
