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
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingCompletedEvent.UnpublishStatus;

import net.sf.json.JSONObject;

public class ProcessUnpublishingCompletedEventTest {

  private CorusHost host;
  private Process   process;
  
  @Before
  public void setUp() throws Exception {
    host    = TestCorusObjects.createHost();
    process = TestCorusObjects.createProcess();
  }

  @Test
  public void testGetProcess() {
    assertThat(event(UnpublishStatus.SUCCESS).getProcess()).isEqualTo(process);
  }

  @Test
  public void testGetStatus() {
    assertThat(event(UnpublishStatus.SUCCESS).getStatus()).isEqualTo(UnpublishStatus.SUCCESS);
  }

  @Test
  public void testGetError_with_exception() {
    assertThat(event(new Exception("Error")).getError().isSet()).isTrue();
  }
  
  @Test
  public void testGetError_without_exception() {
    assertThat(event(UnpublishStatus.FAILURE).getError().isSet()).isFalse();
  }

  @Test
  public void testGetLevel_with_status_success() {
    assertThat(event(UnpublishStatus.SUCCESS).getLevel()).isEqualTo(EventLevel.INFO);
  }
  
  @Test
  public void testGetLevel_with_status_failure() {
    assertThat(event(UnpublishStatus.FAILURE).getLevel()).isEqualTo(EventLevel.ERROR);
  }
  
 @Test
  public void testGetLevel_with_status_not_applicable() {
    assertThat(event(UnpublishStatus.NOT_APPLICABLE).getLevel()).isEqualTo(EventLevel.INFO);
  }


  @Test
  public void testToEventLog() {
    EventLog log = event(UnpublishStatus.SUCCESS).toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Processor.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(ProcessUnpublishingCompletedEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.INFO);

  }

  @Test
  public void testToJsonCorusHostJsonStream_without_exception() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event(UnpublishStatus.SUCCESS).toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("status")).isTrue();
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("process")).isTrue();
  }

  @Test
  public void testToJsonCorusHostJsonStream_with_exception() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event(new Exception("Error")).toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("status")).isTrue();
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("process")).isTrue();
    assertThat(json.has("error")).isTrue();

  }

  private ProcessUnpublishingCompletedEvent event(UnpublishStatus status) {
    return new ProcessUnpublishingCompletedEvent(process, status);
  }
  
  private ProcessUnpublishingCompletedEvent event(Exception error) {
    return new ProcessUnpublishingCompletedEvent(process, error);
  }
}
