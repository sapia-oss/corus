package org.sapia.corus.naming.example;

import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Stateless;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TimeServer implements TimeService, Stateless, java.rmi.Remote{
  
  /**
   * @see org.sapia.corus.naming.example.TimeService#getTime()
   */
  public Date getTime() {
    System.out.println("Returning time...");
    return new Date();
  }
  
  public static void main(String[] args) {
    
    Log.setInfo();
    
    try{
      TimeServer svr = new TimeServer();
      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
      props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "default");
      props.setProperty(Context.PROVIDER_URL, "ubik://localhost:33000");
      InitialContext ctx = new InitialContext(props);
      ctx.bind("timeService", svr);
      System.out.println("Time server bound...");
      while(true){
        Thread.sleep(100000);
      }
    }catch(Throwable t){
      t.printStackTrace();
    }
  }

}
