package org.sapia.corus.naming.example;

import javax.naming.Context;

import org.sapia.ubik.rmi.naming.remote.discovery.DiscoveryHelper;
import org.sapia.ubik.rmi.naming.remote.discovery.JndiDiscoListener;
import org.sapia.ubik.rmi.server.Log;

/**
 * @author Yanick Duchesne
 */
public class DiscoveringTimeServiceClient implements JndiDiscoListener{
  
  public void onJndiDiscovered(Context ctx){
    try{
      System.out.println("Discovered JNDI; looking up service...");
      TimeService ts = (TimeService)ctx.lookup("timeService");
      System.out.println("Time service looked up");      
      System.out.println("Got time: " + ts.getTime());
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    DiscoveryHelper helper = null;
    Log.setInfo();
    try {
      helper = new DiscoveryHelper("default", "224.0.0.1", 5454);
      helper.addJndiDiscoListener(new DiscoveringTimeServiceClient());
      while(true){
        Thread.sleep(1000000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      if(helper != null){
        helper.close();
      }
    }
  }
}
