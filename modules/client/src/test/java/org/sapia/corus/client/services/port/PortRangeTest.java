package org.sapia.corus.client.services.port;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.NoSuchFieldException;

public class PortRangeTest {

  private PortRange range;

  @Before
  public void setUp() throws Exception {
    range = new PortRange("test", 1, 5);
  }

  @Test
  public void testClassDescriptor() throws Exception {
    ClassDescriptor<PortRange> range = new ClassDescriptor<PortRange>(PortRange.class);
    try {
      range.getFieldForName("key");
      Assert.fail("Should not have been able to acquire transient field");
    } catch (NoSuchFieldException e) {
      // ok
    }

    range.getFieldForName("min");
    range.getFieldForName("max");
    range.getFieldForName("available");
    range.getFieldForName("active");
  }

  @Test
  public void testAcquire() throws Exception {
    int port = range.acquire();
    Assert.assertEquals(1, port);
    Assert.assertEquals(4, range.getAvailable().size());
    Assert.assertEquals(1, range.getActive().size());
  }

  @Test
  public void testRelease() throws Exception {
    int port = range.acquire();
    range.release(port);
    Assert.assertEquals(5, range.getAvailable().size());
    Assert.assertEquals(0, range.getActive().size());
  }

  @Test(expected = PortUnavailableException.class)
  public void testAcquireUnavailable() throws Exception {
    for (int i = 1; i <= 5; i++) {
      range.acquire();
    }
    range.acquire();
  }

  @Test
  public void testPortOrdering() throws Exception {
    int port = range.acquire();
    range.release(port);

    List<Integer> available = range.getAvailable();
    for (int i = 1; i <= 5; i++) {
      port = available.get(i - 1);
      Assert.assertEquals(i, port);
    }
  }

}
