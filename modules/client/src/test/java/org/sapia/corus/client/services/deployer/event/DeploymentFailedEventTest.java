package org.sapia.corus.client.services.deployer.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

import net.sf.json.JSONObject;

public class DeploymentFailedEventTest {
  
  private Distribution          dist;
  private CorusHost             host;
  private DeploymentFailedEvent eventWithDist;
  private DeploymentFailedEvent eventWithoutDist;

  @Before
  public void setUp() throws Exception {
    dist             = TestCorusObjects.createDistribution();
    host             = TestCorusObjects.createHost();
    eventWithDist    = new DeploymentFailedEvent(dist);
    eventWithoutDist = new DeploymentFailedEvent(OptionalValue.none());
  }

  @Test
  public void testGetDistribution_with_dist() {
    assertThat(eventWithDist.getDistribution().get()).isEqualTo(dist);
  }

  @Test
  public void testGetDistribution_without_dist() {
    assertThat(eventWithoutDist.getDistribution().isNull()).isTrue();
  }

  
  @Test
  public void testGetLevel() {
    assertThat(eventWithDist.getLevel()).isEqualTo(EventLevel.ERROR);
  }

  @Test
  public void testToEventLog() {
    EventLog log = eventWithDist.toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Deployer.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(DeploymentFailedEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.ERROR);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream_with_dist() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    eventWithDist.toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isNotNull();
    assertThat(json.has("distribution")).isNotNull();
  }
  
  @Test
  public void testToJsonCorusHostJsonStream_without_dist() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    eventWithoutDist.toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isNotNull();
    assertThat(json.has("distribution")).isFalse();
  }

}
