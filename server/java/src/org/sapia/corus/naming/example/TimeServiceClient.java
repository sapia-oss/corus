package org.sapia.corus.naming.example;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Log;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TimeServiceClient {

  public static void main(String[] args) {
    try {
      Log.setInfo();
      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
      props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "default");
      props.setProperty(Context.PROVIDER_URL, "ubik://localhost:33000");
      InitialContext ctx = new InitialContext(props);
      TimeService ts = (TimeService)ctx.lookup("timeService");
      while(true){
        System.out.println("Got time: " + ts.getTime());
        Thread.sleep(2000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
