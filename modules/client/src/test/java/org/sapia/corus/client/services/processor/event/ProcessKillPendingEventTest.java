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
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.Processor;

import net.sf.json.JSONObject;

public class ProcessKillPendingEventTest {

  private CorusHost host;
  private Process   process;
  
  @Before
  public void setUp() throws Exception {
    host    = TestCorusObjects.createHost();
    process = TestCorusObjects.createProcess();
  }

  @Test
  public void testGetProcess() {
    assertThat(event().getProcess()).isEqualTo(process);
  }

  @Test
  public void testGetRequestor() {
    assertThat(event().getRequestor()).isEqualTo(ProcessTerminationRequestor.KILL_REQUESTOR_SERVER);
  }

  @Test
  public void testGetLevel_with_requestor_server() {
    assertThat(event().getLevel()).isEqualTo(EventLevel.CRITICAL);
  }
  
  @Test
  public void testGetLevel_with_requestor_not_server() {
    assertThat(event(ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS).getLevel()).isEqualTo(EventLevel.INFO);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event().toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Processor.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(ProcessKillPendingEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.CRITICAL);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event().toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("requestor")).isTrue();
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("process")).isTrue();
  }

  private ProcessKillPendingEvent event() {
    return event(ProcessTerminationRequestor.KILL_REQUESTOR_SERVER);
  }

  private ProcessKillPendingEvent event(ProcessTerminationRequestor req) {
    return new ProcessKillPendingEvent(req, process);
  }
}
