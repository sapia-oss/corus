package org.sapia.corus.sample.workshop;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.sapia.corus.interop.ConfigurationEvent;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.api.ConfigurationChangeListener;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;
import org.sapia.corus.interop.api.StatusRequestListener;

public class EmbeddedServer {

  private static final String DEFAULT_PORT = "8080";
  
  public static void main(String[] args) throws Exception {
    String portStr = System.getProperty("corus.process.port.http", DEFAULT_PORT);
    System.out.println("Starting server on port: " + portStr);
    final Server server = new Server(Integer.parseInt(portStr));

    server.setStopAtShutdown(true);

    final StatisticsHandler stats = new StatisticsHandler();
    stats.setHandler(new AbstractHandler() {
      
      @Override
      public void handle(
          String target, 
          Request baseRequest,
          HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        PrintWriter writer = response.getWriter();
        writer.println("<html><body><h1>System properties:</h1>");
        
        Set<String> propNames = new TreeSet<String>(System.getProperties().stringPropertyNames());
        for (String n : propNames) {
          writer.println(String.format("%s = %s", n, System.getProperty(n)) + "<br>");
        }
        writer.println("</body></html>");
        writer.flush();
        writer.close();
      }
    });
    
    InteropLink.getImpl().addStatusRequestListener(new StatusRequestListener() {
      
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
    });
    
    InteropLink.getImpl().addShutdownListener(new ShutdownListener() {
      @Override
      public void onShutdown() {
        System.out.println("Process shutdown requested");
        server.destroy();
      }
    });
    
    InteropLink.getImpl().addConfigurationChangeListener(new ConfigurationChangeListener() {
      @Override
      public void onConfigurationChange(ConfigurationEvent event) {
        if (event.getType().equalsIgnoreCase(ConfigurationEvent.TYPE_DELETE)) {
          System.out.println("Properties removed: ");
          for (Param p : event.getParams()) {
            System.out.println(p.getName());
          }
        } else {
          System.out.println("Properties added: ");
          for (Param p : event.getParams()) {
            System.out.println(p.getName() + " = " + p.getValue());
          }          
        }
      }
    });
    
    server.setHandler(stats);
    
    server.start();
    System.out.println("*** Jetty server started successfully ***");
    server.join();
    System.out.println("*** Jetty server stopped ***");
  }
  
  private static Param createParam(String name, Object value){
    return new Param(name, value.toString());
  }
  
}
