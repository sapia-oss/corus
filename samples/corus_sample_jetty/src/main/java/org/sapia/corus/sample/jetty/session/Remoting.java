package org.sapia.corus.sample.jetty.session;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

public class Remoting {

  public static SessionCache lookup() throws NamingException{
    InitialContext context = createInitialContext();
    try{
      return (SessionCache)context.lookup("sessions/cache");
    }finally{
      context.close();
    }
  }
  
  public static void bind(SessionCache cache) throws NamingException{
    InitialContext context = createInitialContext();
    try{
      context.bind("sessions/cache", cache);
    }finally{
      context.close();
    }
  }
  
  
  private static InitialContext createInitialContext() throws NamingException{
    Properties props = new Properties();
    props.setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
    props.setProperty(Context.PROVIDER_URL, "ubik://localhost:33000");
    InitialContext ctx = new InitialContext(props);
    return ctx;
  }
}
