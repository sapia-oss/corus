package org.sapia.corus.client.services.deployer;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;

public class ShellScriptTest {

  private ShellScript script;
  
  @Before
  public void setUp() {
    script = new ShellScript("alias", "fileName", "description");
  }
  
  @Test
  public void testJson() {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    script.toJson(stream);
    ShellScript copy = ShellScript.fromJson(JsonObjectInput.newInstance(writer.toString()));
    
    assertEquals(script.getAlias(), copy.getAlias());
    assertEquals(script.getDescription(), copy.getDescription());
    assertEquals(script.getFileName(), copy.getFileName());
  }
  
  @Test
  public void testSerialization() {
    ShellScript copy = (ShellScript) SerializationUtils.deserialize(SerializationUtils.serialize(script));

    assertEquals(script.getAlias(), copy.getAlias());
    assertEquals(script.getDescription(), copy.getDescription());
    assertEquals(script.getFileName(), copy.getFileName());
  }

}
