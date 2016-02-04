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
import org.sapia.corus.interop.api.ConfigurationChangeListener;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;
import org.sapia.corus.interop.api.StatusRequestListener;
import org.sapia.corus.interop.api.message.ConfigurationEventMessageCommand;
import org.sapia.corus.interop.api.message.ContextMessagePart;
import org.sapia.corus.interop.api.message.InteropMessageBuilderFactory;
import org.sapia.corus.interop.api.message.ParamMessagePart;
import org.sapia.corus.interop.api.message.StatusMessageCommand.Builder;

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
      public void onConfigurationChange(ConfigurationEventMessageCommand event) {
        if (event.getType().equalsIgnoreCase(ConfigurationEventMessageCommand.TYPE_DELETE)) {
          System.out.println("Properties removed: ");
          for (ParamMessagePart p : event.getParams()) {
            System.out.println(p.getName());
          }
        } else {
          System.out.println("Properties added: ");
          for (ParamMessagePart p : event.getParams()) {
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

}
