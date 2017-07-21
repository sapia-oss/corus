package org.sapia.corus.client.services.deployer.event;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

import net.sf.json.JSONObject;

public class RollbackFailedEventTest {
  
  private CorusHost           host;
  private Distribution        distribution;
  private RollbackFailedEvent event;

  @Before
  public void setUp() throws Exception {
    host         = TestCorusObjects.createHost();
    distribution = TestCorusObjects.createDistribution();
    event        = new RollbackFailedEvent(distribution, RollbackType.AUTO);
  }

  @Test
  public void testGetDistribution() {
    assertThat(event.getDistribution()).isEqualTo(distribution);
  }

  @Test
  public void testGetRollbackType() {
    assertThat(event.getRollbackType()).isEqualTo(RollbackType.AUTO);
  }

  @Test
  public void testGetLevel() {
    assertThat(event.getLevel()).isEqualTo(EventLevel.ERROR);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event.toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Deployer.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(RollbackFailedEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.ERROR);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event.toJson(host, stream);

    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("distribution")).isTrue();
    assertThat(json.has("rollbackType")).isTrue();
  }

}
