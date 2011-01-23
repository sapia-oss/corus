package org.sapia.corus.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class NotRespondingApplication {
  
  private static ArrayList _objects = new ArrayList();

  public static void main(String[] args) {
    StdoutInit.init();

    System.out.println("Starting not responding application...");

    Map.Entry prop = null;

    Iterator  props = System.getProperties().entrySet().iterator();

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

    while (true) {
      try {
        _objects.add(new String("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
//        Thread.sleep(250);
      } catch (Throwable e) {
        e.printStackTrace();
        System.out.println(e);
      }
    }
  }
}
