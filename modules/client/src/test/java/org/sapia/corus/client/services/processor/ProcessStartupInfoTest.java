package org.sapia.corus.client.services.processor;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import net.sf.json.JSONObject;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;

public class ProcessStartupInfoTest {
  
  private ProcessStartupInfo info;

  @Before
  public void setUp() throws Exception {
    info = ProcessStartupInfo.forSingleProcess();
  }

  @Test
  public void testForSingleProcess() {
    assertEquals(1, info.getRequestedInstances());
  }

  @Test
  public void testJsonSerialization() {
    StringWriter writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    
    info.toJson(stream, ContentLevel.DETAIL);
    JSONObject json = JSONObject.fromObject(writer.toString());
    ProcessStartupInfo copy = ProcessStartupInfo.fromJson(new JsonObjectInput(json));
    
    assertEquals(info, copy);
  }   

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(info);
    ProcessStartupInfo copy = (ProcessStartupInfo) SerializationUtils.deserialize(payload);
    
    assertEquals(info, copy);
  }

}
