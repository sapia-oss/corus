package org.sapia.corus.configurator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.PropertyMasker;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;

/**
 * implements an http extension to provide read access to the internal
 * configuration properties.
 * 
 * @author jcdesrochers
 */
public class ConfiguratorHttpExtension implements HttpExtension {

  public static final String CONTEXT_PATH = "config";

  private Configurator configurator;

  private ServerContext serverContext;

  /**
   * Creates a new {@link ConfiguratorHttpExtension} instance.
   * 
   * @param aConfigurator
   *          The configurator instance.
   * @param aContext
   *          The server context.
   */
  public ConfiguratorHttpExtension(Configurator aConfigurator, ServerContext aContext) {
    configurator = aConfigurator;
    serverContext = aContext;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.sapia.corus.client.services.http.HttpExtension#getInfo()
   */
  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Configurator");
    info.setDescription("Allows viewing the <a href=\"" + CONTEXT_PATH + "\">configuration</a> of the corus server (process & server properties)");
    return info;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sapia.corus.client.services.http.HttpExtension#process(org.sapia.corus
   * .client.services.http.HttpContext)
   */
  @Override
  public void process(HttpContext httpContext) throws Exception, FileNotFoundException {
    httpContext.getResponse().setHeader("Content-Type", "text/html");
    httpContext.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);

    PrintStream responseStream = new PrintStream(httpContext.getResponse().getOutputStream());
    responseStream.println("<html><title>Corus Configurator</title><head>" + HttpExtensionManager.STYLE_HEADER + "</head><body>");

    responseStream.println("<h3>Process Properties</h3>");
    generatePropertiesTable(configurator.getAllPropertiesList(PropertyScope.PROCESS, new HashSet<ArgMatcher>()), responseStream);
    responseStream.println("<br/>");

    responseStream.println("<h3>Server Properties</h3>");
    generatePropertiesTable(serverContext.getCorusProperties(), responseStream);
    responseStream.println("<br/>");
    
    responseStream.println("<h3>Stored Server Properties</h3>");
    generatePropertiesTable(configurator.getAllPropertiesList(PropertyScope.SERVER, new HashSet<ArgMatcher>()), responseStream);
    responseStream.println("<br/>");

    responseStream.println("<h3>Tag Configuration</h3>");
    generateValueTable(configurator.getTags(), responseStream);
    responseStream.println("<br/>");

    responseStream.println(HttpExtensionManager.FOOTER + "</body></html></body></html>");
    responseStream.flush();
    responseStream.close();
  }
  
  @Override
  public void destroy() {
  }

  /**
   * Internal method that generate an html table of some properties.
   * 
   * @param someProps
   *          The properties to output.
   * @param output
   *          The stream in which to send the output
   */
  protected void generatePropertiesTable(Properties someProps, PrintStream output) {
    output.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"75%\">");
    output.println("<th width=\"60%\">Name</th><th width=\"40%\">Value</th>");
    
    List<String> keys = new ArrayList<String>();
    for (String n : someProps.stringPropertyNames()) {
      keys.add(n);
    }
    Collections.sort(keys);
    PropertyMasker masker = configurator.getPropertyMasker();
    for (String k : keys) {
      String value = someProps.getProperty(k);
      output.println("<tr valign=\"top\"><td>" + k + "</td><td>" 
      + masker.getMaskedValue(k, value) + "</td></tr>");
    }
    
    output.println("</table>");
  }
  
  /**
   * Internal method that generate an html table of some properties.
   * 
   * @param someProps
   *          The properties to output.
   * @param output
   *          The stream in which to send the output
   */
  protected void generatePropertiesTable(List<Property> someProps, PrintStream output) {
    output.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"75%\">");
    output.println("<th width=\"60%\">Name</th><th width=\"30%\">Value</th><th width=\"10%\">Category</th>");
    PropertyMasker masker = configurator.getPropertyMasker();
    for (Property p : someProps) {
      output.println("<tr valign=\"top\"><td>" + p.getName() + "</td><td>" 
      + masker.getMaskedValue(p.getName(), p.getValue()) + "</td>"
      + "<td>" + (p.getCategory().isNull() ? "N/A" : p.getCategory().get()) + "</tr>");
    }
    
    output.println("</table>");
  }

  /**
   * Internal method that generate an html table of values.
   * 
   * @param someValues
   *          The values to output.
   * @param output
   *          The stream in which to send the output
   */
  protected void generateValueTable(Collection<?> someValues, PrintStream output) {
    List<String> sortedValues = new ArrayList<String>();
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
