package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

public class CliUtilsTest {

  @Test
  public void testParseServerAddresses() {
    Set<ServerAddress> addresses = CliUtils.parseServerAddresses("host1:33000,host2:33000");
    assertEquals(2, addresses.size());
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("host1", 33000)));
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("host2", 33000)));
  }
  
  @Test
  public void testCollectResultsPerHost() {
    Results<String> results = new Results<String>();
    results.setInvocationCount(5);
    for (int i = 0; i < 5; i++) {
      Result<String> r = new Result<String>(new HttpAddress(Uri.parse("http://test_" + i)), "data_i");
      results.addResult(r);
    }
    
    Map<ServerAddress, String> resultsPerHost = CliUtils.collectResultsPerHost(results);
    for (int i = 0; i < 5; i++) {
      assertEquals(resultsPerHost.get(new HttpAddress(Uri.parse("http://test_" + i))), "data_i");
    }    
  }
}
