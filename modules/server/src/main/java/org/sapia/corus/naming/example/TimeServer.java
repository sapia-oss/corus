package org.sapia.corus.naming.example;

import java.util.Date;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Stateless;

/**
 * @author Yanick Duchesne
 */
public class TimeServer implements TimeService, Stateless, java.rmi.Remote {

  /**
   * @see org.sapia.corus.naming.example.TimeService#getTime()
   */
  public Date getTime() {
    System.out.println("Returning time...");
    return new Date();
  }

  public static void main(String[] args) {

    try {
      TimeServer svr = new TimeServer();
      Properties props = new Properties();
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
      props.setProperty(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, "yduchesne");
      props.setProperty(Context.PROVIDER_URL, "ubik://localhost:33000");
      InitialContext ctx = new InitialContext(props);
      ctx.bind("timeService", svr);
      System.out.println("Time server bound...");
      while (true) {
        Thread.sleep(100000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
