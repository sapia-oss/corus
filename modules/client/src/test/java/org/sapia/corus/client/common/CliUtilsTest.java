package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

public class CliUtilsTest {

  @Test
  public void testParseServerAddresses() {
    Set<ServerAddress> addresses = CliUtils.parseServerAddresses("host1:33000,host2:33000");
    assertEquals(2, addresses.size());
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("host1", 33000)));
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("host2", 33000)));
  }

}
