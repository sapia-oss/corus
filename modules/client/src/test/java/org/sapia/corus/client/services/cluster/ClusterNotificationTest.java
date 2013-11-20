package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.TCPAddress;

public class ClusterNotificationTest {

  private ClusterNotification notification;

  private Endpoint target;

  @Before
  public void setUp() {
    notification = new ClusterNotification() {
      @Override
      public String getEventType() {
        return "test";
      }
    };

    target = new Endpoint(new TCPAddress("test", "host1", 1), new TCPAddress("test", "host1", 2));
  }

  @Test
  public void testIsTargetedEmptyTargets() {
    assertTrue(notification.isTargeted(target));
  }

  @Test
  public void testIsTargeted() {
    notification.addTarget(target);
    assertTrue(notification.isTargeted(target));
  }

  @Test
  public void testAddVisited() {
    notification.addTarget(target);
    assertTrue(notification.getTargets().contains(target));
    notification.addVisited(target);
    assertFalse(notification.getTargets().contains(target));
    assertTrue(notification.getVisited().contains(target));
  }
}
