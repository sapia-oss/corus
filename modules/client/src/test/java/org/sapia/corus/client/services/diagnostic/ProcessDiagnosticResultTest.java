package org.sapia.corus.client.services.diagnostic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import net.sf.json.JSONObject;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;

public class ProcessDiagnosticResultTest {
  
  private ProcessDiagnosticResult result;

  @Before
  public void setUp() throws Exception {
    Process p = new Process(new DistributionInfo("dist", "1.0" , "testProfile", "testProcess"));
    p.addActivePort(new ActivePort("testPort", 8080));
    p.setProcessDir("testDir");
    result = new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "successful", p, "test", new ActivePort("testPort", 8080));
  }

  @Test
  public void testToJson() {
    StringWriter writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    result.toJson(stream, ContentLevel.DETAIL);
    JSONObject json = JSONObject.fromObject(writer.toString());
    
    assertEquals(ProcessDiagnosticStatus.CHECK_SUCCESSFUL.name(), json.getString("status"));
    assertEquals("successful", json.getString("message"));
    
    JSONObject portJson = json.getJSONObject("diagnosticPort");
    assertEquals("testPort", portJson.getString("name"));
    assertEquals(8080, portJson.getInt("value"));
    
    JSONObject processJson = json.getJSONObject("process");
    
    assertTrue(!processJson.isNullObject());
  }

  @Test
  public void testSerialization() {
    byte[] payload = SerializationUtils.serialize(result);
    
    ProcessDiagnosticResult copy = (ProcessDiagnosticResult) SerializationUtils.deserialize(payload);
    
    assertEquals(result, copy);
  }

}
