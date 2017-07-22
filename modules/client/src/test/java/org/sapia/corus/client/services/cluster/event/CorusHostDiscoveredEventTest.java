package org.sapia.corus.client.services.cluster.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

import net.sf.json.JSONObject;

public class CorusHostDiscoveredEventTest {
  
  private CorusHost                host;
  private CorusHostDiscoveredEvent event;

  @Before
  public void setUp() throws Exception {
    host  = TestCorusObjects.createHost();
    event = new CorusHostDiscoveredEvent(host);
  }

  @Test
  public void testGetHost() {
    assertThat(event.getHost()).isEqualTo(host);
  }

  @Test
  public void testGetLevel() {
    assertThat(event.getLevel()).isEqualTo(EventLevel.TRACE);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event.toEventLog();
    
    assertThat(log.getSource()).isEqualTo(ClusterManager.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(CorusHostDiscoveredEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.TRACE);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event.toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("discoveredHost")).isTrue();
    
  }

}
