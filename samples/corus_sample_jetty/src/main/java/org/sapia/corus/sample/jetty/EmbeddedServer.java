package org.sapia.corus.sample.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.StatusRequestListener;
import org.sapia.corus.interop.api.message.ContextMessagePart;
import org.sapia.corus.interop.api.message.InteropMessageBuilderFactory;
import org.sapia.corus.interop.api.message.StatusMessageCommand.Builder;

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
  public void onStatus(Builder statusBuilder, InteropMessageBuilderFactory factory) {
    ContextMessagePart.Builder contextBuilder = factory.newContextBuilder().name("org.sapia.corus.sample.jetty");
    contextBuilder
      .param("dispatched", String.valueOf(stats.getDispatched()))
      .param("dispatchedActive", String.valueOf(stats.getDispatchedActive()))
      .param("dispatchedActiveMax", String.valueOf(stats.getDispatchedActiveMax()))
      .param("dispatchedTimeMax", String.valueOf(stats.getDispatchedTimeMax()))
      .param("dispatchedTimeTotal", String.valueOf(stats.getDispatchedTimeTotal()))
      .param("dispatchedTimeMean", String.valueOf(stats.getDispatchedTimeMean()))
      .param("requests", String.valueOf(stats.getRequests()))
      .param("requestsActive", String.valueOf(stats.getRequestsActive()))
      .param("requestsActiveMax", String.valueOf(stats.getRequestsActiveMax()))
      .param("requestsTimeMax", String.valueOf(stats.getRequestTimeMax()))
      .param("requestsTimeMean", String.valueOf(stats.getRequestTimeMean()))
      .param("requestsTimeTotal", String.valueOf(stats.getRequestTimeTotal()))
      .param("suspends", String.valueOf(stats.getSuspends()))
      .param("suspendsActive", String.valueOf(stats.getSuspendsActive()))
      .param("suspendsActiveMax", String.valueOf(stats.getSuspendsActiveMax()));
    statusBuilder.context(contextBuilder.build());        
  }
}
