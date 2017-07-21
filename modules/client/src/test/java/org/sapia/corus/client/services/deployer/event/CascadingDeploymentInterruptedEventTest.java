package org.sapia.corus.client.services.deployer.event;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.ubik.net.ServerAddress;

import net.sf.json.JSONObject;

public class CascadingDeploymentInterruptedEventTest {
  
  private ServerAddress                       currentHost;
  private Set<ServerAddress>                  remainingHosts;
  private CascadingDeploymentInterruptedEvent event;

  @Before
  public void setUp() throws Exception {
    currentHost    = TestCorusObjects.createAddress();
    remainingHosts = new HashSet<>(Arrays.asList(TestCorusObjects.createAddress()));
    event          = new CascadingDeploymentInterruptedEvent(currentHost, remainingHosts);
  }

  @Test
  public void testGetCurrentHost() {
    assertThat(event.getCurrentHost()).isEqualTo(currentHost);
  }

  @Test
  public void testGetRemainingHosts() {
    assertThat(event.getRemainingHosts()).containsAll(remainingHosts);
  }

  @Test
  public void testGetLevel() {
    assertThat(event.getLevel()).isEqualTo(EventLevel.ERROR);
  }

  @Test
  public void testToEventLog() {
    EventLog log = event.toEventLog();
    
    assertThat(log.getLevel()).isEqualTo(EventLevel.ERROR);
    assertThat(log.getMessage()).isNotEmpty();
    assertThat(log.getSource()).isEqualTo(Deployer.class.getSimpleName());
    assertThat(log.getType()).isEqualTo(CascadingDeploymentInterruptedEvent.class.getSimpleName());
    assertThat(log.getTime()).isNotNull();
  }

  @Test
  public void testToJsonCorusHostJsonStream() {
    StringWriterJsonStream stream = new StringWriterJsonStream();
    event.toJson(TestCorusObjects.createHost(), stream);
    
    JSONObject json = stream.toJsonObject();
    
    assertThat(json.has("remainingHosts")).isTrue();
    assertThat(json.has("message")).isTrue();
  }

}
