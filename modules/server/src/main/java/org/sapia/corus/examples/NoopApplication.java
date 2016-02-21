package org.sapia.corus.examples;

import java.util.Iterator;
import java.util.Map;

import org.sapia.corus.interop.api.ConfigurationChangeListener;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;
import org.sapia.corus.interop.api.message.ConfigurationEventMessageCommand;
import org.sapia.corus.interop.api.message.ParamMessagePart;

/**
 * @author Yanick Duchesne
 * 
 *         <dl>
 *         <dt><b>Copyright:</b>
 *         <dd>Copyright &#169; 2002-2003 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All
 *         Rights Reserved.</dd></dt>
 *         <dt><b>License:</b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page</a> at the
 *         Sapia OSS web site</dd></dt>
 *         </dl>
 */
public class NoopApplication {
  public static void main(String[] args) {

    System.out.println("Starting noop application...");

    Map.Entry prop = null;

    Iterator props = System.getProperties().entrySet().iterator();

    System.out.println("=================================================");

    while (props.hasNext()) {
      prop = (Map.Entry) props.next();
      System.out.println(prop.getKey() + " = " + prop.getValue());
    }

    System.out.println("=================================================");

    ShutdownListener shutdListener = new ShutdownListener() {
      public void onShutdown() {
        System.out.println("Shutting down...");
      }
    };
    
    ConfigurationChangeListener cfgListener = new ConfigurationChangeListener() {
      
      @Override
      public void onConfigurationChange(ConfigurationEventMessageCommand event) {
        System.out.println("Config change detected:");
        for (ParamMessagePart p : event.getParams()) {
          System.out.println(String.format("%s = %s", p.getName(), p.getValue()));
        }
      }
    };
 
    InteropLink.getImpl().addShutdownListener(shutdListener);
    InteropLink.getImpl().addConfigurationChangeListener(cfgListener);

    while (true) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();

        break;
      }
    }
  }
}
