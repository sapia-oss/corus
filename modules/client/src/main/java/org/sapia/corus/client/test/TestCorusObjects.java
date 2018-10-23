package org.sapia.corus.client.test;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;

import java.security.PublicKey;
import java.util.Random;

/**
 * Factory class for creating dummy {@link CorusHost} instances.
 * 
 * @author yduchesne
 *
 */
public final class TestCorusObjects {


  public static class TestPubKey implements PublicKey {
    @Override
    public byte[] getEncoded() {
      return new byte[0];
    }

    @Override
    public String getAlgorithm() {
      return "TEST_ALGO";
    }

    @Override
    public String getFormat() {
      return "TEST_FORMAT";
    }

  }

  // ==========================================================================
  
  private TestCorusObjects() {
    
  }
  
  /**
   * @return a dummy {@link CorusHost} instance to be used for testing.
   */
  public static CorusHost createHost() {
    Endpoint ep = new Endpoint(new TcpSocketAddress("localhost", 33000), new TcpSocketAddress("localhost", 12345));
    return CorusHost.newInstance("test-node", ep, "test-os", "test-jvm", new TestPubKey());
  }
 
  public static TcpSocketAddress createAddress() {
    return createAddress("localhost", new Random().nextInt(65534 - 1024) + 1024);
  }
  
  public static TcpSocketAddress createAddress(String host, int port) {
    return new TcpSocketAddress(host, port);
  }
  
  public static Distribution createDistribution() {
    return new Distribution("test-dist", "1.0");
  }
  
  public static Distribution createDistribution(String name, String version) {
    return new Distribution(name, version);
  }
  
  public static Process createProcess() {
    return new Process(new DistributionInfo("test-dist", "1.0", "test-profile", "test-process"), "12345");
  }
  
  public static ProcessConfig createProcessConfig() {
    return new ProcessConfig("test-process");
  }
  
}
