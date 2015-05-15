package org.sapia.corus.examples;

import java.util.Iterator;
import java.util.Map;

import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;

public class ShutdownApplication {

  public static void main(String[] args) {

    System.out.println("Starting restart application...");

    Map.Entry prop = null;

    Iterator props = System.getProperties().entrySet().iterator();

    System.out.println("=================================================");

    while (props.hasNext()) {
      prop = (Map.Entry) props.next();
      System.out.println(prop.getKey() + " = " + prop.getValue());
    }

    System.out.println("=================================================");

    ShutdownListener listener = new ShutdownListener() {
      public void onShutdown() {
        System.out.println("Shutting down...");
      }
    };

    InteropLink.getImpl().addShutdownListener(listener);

    try {
      Thread.sleep(20000);
      InteropLink.getImpl().shutdown();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}