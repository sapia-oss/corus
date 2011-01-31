package org.sapia.corus.sample;

import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;

public class EchoMain {

  public static void main(String[] args) {
    if (args.length != 3) {
      throw new IllegalArgumentException("Expecting 3 arguments: {name} {initWaitDelayMillis} {loopIntervalMillis}");
    }
    
    final EchoServer server = new EchoServer();
    server.setName(args[0]);
    server.setInitialWaitDelayMillis(Long.valueOf(args[1]));
    server.setLoopIntervalMillis(Long.valueOf(args[2]));
    server.start();
    
    // shutdown hook on corus interop
    InteropLink.getImpl().addShutdownListener(new ShutdownListener() {
      public void onShutdown() {
        server.stop();
      }
    });
    
    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
