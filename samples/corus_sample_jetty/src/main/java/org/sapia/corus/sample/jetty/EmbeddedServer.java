package org.sapia.corus.sample.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.StatusRequestListener;

public class EmbeddedServer implements StatusRequestListener{

  private int port;
  private SessionManager sessionManager;
  private StatisticsHandler stats;
  
  public EmbeddedServer(int port) {
    this(port, null);
  }
  
  public EmbeddedServer(int port, SessionManager sessionManager) {
    this.port = port;
    this.sessionManager = sessionManager;
  }
  
  public void start() throws Exception{
    Server server = new Server(port);
    server.setStopAtShutdown(true);
    stats = new StatisticsHandler();
    if(sessionManager != null){
      SessionHandler sessionHandler = new SessionHandler(sessionManager);
      stats.setHandler(ServerUtil.loadWebapps(sessionHandler));
    }
    else{
      stats.setHandler(ServerUtil.loadWebapps());
    }
    server.setHandler(stats);
    
    server.start();
    
    InteropLink.getImpl().addStatusRequestListener(this);
    
    System.out.println("*** Jetty server started successfully ***");
    server.join();
    System.out.println("*** Jetty server stopped ***");
  }

  @Override
  public void onStatus(Status status) {
    Context context = new Context("org.sapia.corus.sample.jetty");
    context.addParam(createParam("dispatched", stats.getDispatched()));        
    context.addParam(createParam("dispatchedActive", stats.getDispatchedActive()));
    context.addParam(createParam("dispatchedActiveMax", stats.getDispatchedActiveMax()));        
    context.addParam(createParam("dispatchedTimeMax", stats.getDispatchedTimeMax()));
    context.addParam(createParam("dispatchedTimeTotal", stats.getDispatchedTimeTotal()));
    context.addParam(createParam("dispatchedTimeMean", stats.getDispatchedTimeMean()));
    context.addParam(createParam("requests", stats.getRequests()));        
    context.addParam(createParam("requestsActive", stats.getRequestsActive()));        
    context.addParam(createParam("requestsActiveMax", stats.getRequestsActiveMax()));        
    context.addParam(createParam("requestsTimeMax", stats.getRequestTimeMax()));        
    context.addParam(createParam("requestsTimeMean", stats.getRequestTimeMean()));
    context.addParam(createParam("requestsTimeTotal", stats.getRequestTimeTotal()));
    context.addParam(createParam("suspends", stats.getSuspends()));        
    context.addParam(createParam("suspendsActive", stats.getSuspendsActive()));        
    context.addParam(createParam("suspendsActiveMax", stats.getSuspendsActiveMax()));
    status.addContext(context);
  }
  
  private Param createParam(String name, Object value){
    return new Param(name, value.toString());
  }
  
}
