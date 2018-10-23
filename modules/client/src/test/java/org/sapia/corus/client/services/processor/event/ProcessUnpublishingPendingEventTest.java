package org.sapia.corus.client.services.processor.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

import net.sf.json.JSONObject;

public class ProcessUnpublishingPendingEventTest {

  private CorusHost                       host;
  private Process                         process;
  private ProcessUnpublishingPendingEvent event;
  
  @Before
  public void setUp() throws Exception {
    host    = TestCorusObjects.createHost();
    process = TestCorusObjects.createProcess();
    event   = new ProcessUnpublishingPendingEvent(process);
  }

  @Test
  public void testGetProcess() {
    assertThat(event.getProcess()).isEqualTo(process);
  }

  @Test
  public void testGetLevel() {
    assertThat(event.getLevel()).isEqualTo(EventLevel.INFO);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event.toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Processor.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(ProcessUnpublishingPendingEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.INFO);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event.toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("process")).isTrue();
  }

}
