package org.sapia.corus.configurator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;

/**
 * implements an http extension to provide read access to the internal configuration properties.
 * 
 * @author jcdesrochers
 */
public class ConfiguratorHttpExtension implements HttpExtension {

  public static final String CONTEXT_PATH   = "config";

  private Configurator configurator;
  
  private ServerContext serverContext;
  
  /**
   * Creates a new {@link ConfiguratorHttpExtension} instance.
   * 
   * @param aConfigurator The configurator instance.
   * @param aContext The server context.
   */
  public ConfiguratorHttpExtension(Configurator aConfigurator, ServerContext aContext) {
    configurator = aConfigurator;
    serverContext = aContext;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.client.services.http.HttpExtension#getInfo()
   */
  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Configurator");
    info.setDescription("Allows viewing the <a href=\""+ CONTEXT_PATH + "\">configuration</a> of the corus server (process & server properties)");
    return info;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.services.http.HttpExtension#process(org.sapia.corus.client.services.http.HttpContext)
   */
  @Override
  public void process(HttpContext httpContext) throws Exception, FileNotFoundException {
    httpContext.getResponse().setHeader("Content-Type", "text/html");
    httpContext.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);
    
    PrintStream responseStream = new PrintStream(httpContext.getResponse().getOutputStream());
    responseStream.println("<html><title>Corus Configurator</title><body>");

    responseStream.println("<h3>Process Configuration Properties</h3>");
    generatePropertiesTable(configurator.getProperties(PropertyScope.PROCESS), responseStream);
    responseStream.println("<br/>");

    responseStream.println("<h3>Server Configuration Properties</h3>");
    generatePropertiesTable(configurator.getProperties(PropertyScope.SERVER), responseStream);
    responseStream.println("<br/>");
    
    responseStream.println("<h3>Tag Configuration</h3>");
    generateValueTable(configurator.getTags(), responseStream);
    responseStream.println("<br/>");

    responseStream.println("<h3>Corus Properties</h3>");
    generatePropertiesTable(serverContext.getCorusProperties(), responseStream);
    responseStream.println("<br/>");
    
    responseStream.println(HttpExtensionManager.FOOTER  +"</body></html></body></html>");
    responseStream.flush();
    responseStream.close();
  }

  /**
   * Internal method that generate an html table of some properties.
   *  
   * @param someProps The properties to output.
   * @param output The stream in which to send the output 
   */
  protected void generatePropertiesTable(Properties someProps, PrintStream output) {
    List<String> keys = new ArrayList<>();
    for (Object key: someProps.keySet()) {
      keys.add((String) key);
    }
    Collections.sort(keys);

    output.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"75%\">");
    output.println("<th width=\"50%\">Name</th><th width=\"50%\">Value</th>");
    for (String name: keys) {
      String value = someProps.getProperty(name);
      output.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
    }
    
    output.println("</table>");
  }
  
  /**
   * Internal method that generate an html table of values.
   *  
   * @param someValues The values to output.
   * @param output The stream in which to send the output 
   */
  protected void generateValueTable(Collection<?> someValues, PrintStream output) {
    List<String> sortedValues = new ArrayList<>();
    for (Object value: someValues) {
      sortedValues.add(String.valueOf(value));
    }
    Collections.sort(sortedValues);

    output.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"75%\">");
    output.println("<th>Value</th>");
    for (String value: sortedValues) {
      output.println("<tr align=\"left\"><td>" + value + "</td></tr>");
    }
    
    output.println("</table>");
  }

}
