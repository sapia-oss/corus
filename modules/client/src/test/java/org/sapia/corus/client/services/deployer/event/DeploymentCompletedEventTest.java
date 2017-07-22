package org.sapia.corus.client.services.deployer.event;

import static org.assertj.core.api.Assertions.assertThat;

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

public class DeploymentCompletedEventTest {
  
  private Distribution             dist;
  private CorusHost                host;
  private DeploymentCompletedEvent event;
  

  @Before
  public void setUp() throws Exception {
    dist  = TestCorusObjects.createDistribution();
    event = new DeploymentCompletedEvent(dist);
    host  = TestCorusObjects.createHost();
  }

  @Test
  public void testGetDistribution() {
    assertThat(event.getDistribution()).isEqualTo(dist);
  }

  @Test
  public void testGetLevel() {
    assertThat(event.getLevel()).isEqualTo(EventLevel.INFO);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event.toEventLog();
    
    assertThat(log.getSource()).isEqualTo(Deployer.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(DeploymentCompletedEvent.class.getSimpleName());
    assertThat(log.getLevel()).isEqualTo(EventLevel.INFO);
    assertThat(log.getMessage()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    
    event.toJson(host, stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("message")).isTrue();
    assertThat(json.has("distribution")).isTrue();
  }

}
