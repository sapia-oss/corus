package org.sapia.corus.client.services.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
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
  
  @Test
  public void testJson() throws Exception {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    range.acquire();
    range.acquire();
    range.toJson(stream, ContentLevel.DETAIL);
    PortRange copy = PortRange.fromJson(JsonObjectInput.newInstance(writer.toString()));
    
    assertEquals(range.getName(), copy.getName());
    assertEquals(range.getAvailable().size(), copy.getAvailable().size());
    assertTrue(range.getAvailable().containsAll(copy.getAvailable()));
    assertEquals(range.getActive().size(), copy.getActive().size());
    assertTrue(range.getActive().containsAll(copy.getActive()));
    assertEquals(range.getMin(), copy.getMin());
    assertEquals(range.getMax(), copy.getMax());
  }
  
  @Test
  public void testSerialization() {
    PortRange copy = (PortRange) SerializationUtils.deserialize(SerializationUtils.serialize(range));

    assertEquals(range.getName(), copy.getName());
    assertEquals(range.getAvailable().size(), copy.getAvailable().size());
    assertTrue(range.getAvailable().containsAll(copy.getAvailable()));
    assertEquals(range.getActive().size(), copy.getActive().size());
    assertTrue(range.getActive().containsAll(copy.getActive()));
    assertEquals(range.getMin(), copy.getMin());
    assertEquals(range.getMax(), copy.getMax());
  }

}
