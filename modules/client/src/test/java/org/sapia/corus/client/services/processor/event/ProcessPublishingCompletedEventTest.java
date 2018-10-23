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
import org.sapia.corus.client.services.processor.event.ProcessPublishingCompletedEvent.PublishStatus;

import net.sf.json.JSONObject;

public class ProcessPublishingCompletedEventTest {

  private CorusHost host;
  private Process   process;
  
  @Before
  public void setUp() throws Exception {
    host    = TestCorusObjects.createHost();
    process = TestCorusObjects.createProcess();
  }

  @Test
  public void testGetProcess() {
    assertThat(event(PublishStatus.SUCCESS).getProcess()).isEqualTo(process);
  }

  @Test
  public void testGetStatus() {
    assertThat(event(PublishStatus.SUCCESS).getStatus()).isEqualTo(PublishStatus.SUCCESS);
  }

  @Test
  public void testGetError_with_exception() {
    assertThat(event(new Exception("Error")).getError().isSet()).isTrue();
  }
  
  @Test
  public void testGetError_without_exception() {
    assertThat(event(PublishStatus.FAILURE).getError().isSet()).isFalse();
  }

  @Test
  public void testGetLevel_with_status_success() {
    assertThat(event(PublishStatus.SUCCESS).getLevel()).isEqualTo(EventLevel.INFO);
  }
  
  @Test
  public void testGetLevel_with_status_failure() {
    assertThat(event(PublishStatus.FAILURE).getLevel()).isEqualTo(EventLevel.ERROR);
  }
  
  @Test
  public void testGetLevel_with_status_max_attempts_reached() {
    assertThat(event(PublishStatus.MAX_ATTEMPTS_REACHED).getLevel()).isEqualTo(EventLevel.ERROR);
  }
  
  @Test
  public void testGetLevel_with_status_not_applicable() {
    assertThat(event(PublishStatus.NOT_APPLICABLE).getLevel()).isEqualTo(EventLevel.INFO);
  }


  @Test
  public void testToEventLog() {
    EventLog log = event(PublishStatus.SUCCESS).toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Processor.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(ProcessPublishingCompletedEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.INFO);

  }

  @Test
  public void testToJsonCorusHostJsonStream_without_exception() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event(PublishStatus.SUCCESS).toJson(host, stream);
    
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

  private ProcessPublishingCompletedEvent event(PublishStatus status) {
    return new ProcessPublishingCompletedEvent(process, status);
  }
  
  private ProcessPublishingCompletedEvent event(Exception error) {
    return new ProcessPublishingCompletedEvent(process, error);
  }

}
