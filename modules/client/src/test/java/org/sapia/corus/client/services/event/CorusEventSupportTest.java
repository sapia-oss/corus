package org.sapia.corus.client.services.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.ThreadSafeDateFormatter;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;

import net.sf.json.JSONObject;

@RunWith(MockitoJUnitRunner.class)
public class CorusEventSupportTest {

  @Mock
  private PublicKey         pubKey;
  private CorusHost         host;
  private CorusEventSupport support;
 
  @Before
  public void setUp() throws Exception {
    support = new CorusEventSupport() {
      @Override
      public EventLog toEventLog() {
        return EventLog.builder()
            .source(source())
            .type(CorusEventSupport.class)
            .level(getLevel())
            .message("test-%s", "1")
            .build();
      }
      
      @Override
      public EventLevel getLevel() {
        return EventLevel.INFO;
      }
      
      @Override
      protected void toJson(JsonStream stream) {
        stream.field("message").value("test");
      }
      
      @Override
      protected Class<?> source() {
        return CorusEventSupportTest.class;
      }
      
      @Override
      protected Class<?> type() {
        return CorusEventSupport.class;
      }
    };
    
    host = TestCorusObjects.createHost();
  }

  @Test
  public void testToJsonCorusHostJsonStream() throws Exception {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    support.toJson(host, stream);

    JSONObject json = JSONObject.fromObject(stream.toString());
    
    assertThat(json.has("source")).isTrue();
    assertThat(json.getString("source")).isEqualTo(CorusEventSupportTest.class.getSimpleName());
    
    assertThat(json.has("type")).isTrue();
    assertThat(json.getString("type")).isEqualTo(CorusEventSupport.class.getSimpleName());
 
    assertThat(json.has("level")).isTrue();
    assertThat(json.getString("level")).isEqualTo(EventLevel.INFO.name());
    
    assertThat(json.has("time")).isTrue();
    ThreadSafeDateFormatter.getIsoUtcInstance().parse(json.getString("time"));
    
    assertThat(json.has("message")).isTrue();
    assertThat(json.getString("message")).isEqualTo("test");
    
    assertThat(json.has("host")).isTrue();
    
  }
  

}
