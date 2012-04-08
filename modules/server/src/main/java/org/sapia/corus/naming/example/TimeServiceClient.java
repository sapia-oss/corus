package org.sapia.corus.naming.example;

import java.lang.reflect.UndeclaredThrowableException;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.RemoteRuntimeException;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

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
      TimeService ts = lookup();

      while(true){
        try{
          System.out.println("Got time: " + ts.getTime());
          Thread.sleep(8000);
        }catch(RemoteRuntimeException e){
          Thread.sleep(8000);
          // retry
        }catch(UndeclaredThrowableException e){
          if(e.getUndeclaredThrowable() instanceof RemoteException){
            Thread.sleep(8000);
            System.out.println("Server down; will retry");
          }
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  private static TimeService lookup() throws Exception{
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "default");
    props.setProperty(Context.PROVIDER_URL, "ubik://localhost:33000");
    InitialContext ctx = new InitialContext(props);
    return (TimeService)ctx.lookup("timeService");
  }
}
