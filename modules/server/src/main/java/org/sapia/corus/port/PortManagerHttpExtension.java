package org.sapia.corus.port;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.http.HttpExtensionManager;

/**
 * This class implements an http extension to provide read access to
 * the internal port manager status.
 * 
 * @author jcdesrochers
 */
public class PortManagerHttpExtension implements HttpExtension {

  public static final String CONTEXT_PATH = "port";

  /** The internal port manager on which this http extension reports. */
  private PortManager portManager;
  
  /**
   * Creates a new {@link PortManagerHttpExtension} instance.
   * 
   * @param aPortManager The port manager instance to report. 
   */
  public PortManagerHttpExtension(PortManager aPortManager) {
    portManager = aPortManager;
  }
  
  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Port Management");
    info.setDescription("Allows viewing the <a href=\"" + CONTEXT_PATH + "\">port range</a> configuration and usage");
    return info;
  }

  @Override
  public void process(HttpContext httpContext) throws Exception, FileNotFoundException {
    httpContext.getResponse().setHeader("Content-Type", "text/html");
    httpContext.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);

    PrintStream responseStream = new PrintStream(httpContext.getResponse().getOutputStream());
    responseStream.println("<html><title>Corus Port Management</title><head>" + HttpExtensionManager.STYLE_HEADER + "</head><body>");

    responseStream.println("<h3>Process Configuration Properties</h3>");
    responseStream.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"75%\">");
    responseStream.println("<th width=\"40%\">Name</th><th width=\"20%\" align=\"center\">Range</th><th width=\"40%\" align=\"center\">Status</th>");
    for (PortRange range: portManager.getPortRanges()) {
      List<PortRangeEntry> entries = new ArrayList<PortRangeEntry>();
      for (Integer number: range.getAvailable()) {
        entries.add(PortRangeEntry.createNewAvailable(number));
      }
      for (Integer number: range.getActive()) {
        entries.add(PortRangeEntry.createNewActive(number));
      }
      Collections.sort(entries);

      StringBuilder rangeBuilder = new StringBuilder();
      StringBuilder statusBuilder = new StringBuilder();
      for (PortRangeEntry portEntry: entries) {
        rangeBuilder.append(String.valueOf(portEntry.portNumber)).append("<br/>");
        statusBuilder.append(portEntry.isAvailable? "Available": "Active").append("<br/>");
      }
      
      responseStream.println("<tr valign=\"top\"><td>" + range.getName() + "</td><td align=\"center\">" + rangeBuilder.toString() + "</td><td>" + statusBuilder.toString() + "</td></tr>");
    }
    
    responseStream.println("</table>");
    responseStream.println("<br/>");

    responseStream.println(HttpExtensionManager.FOOTER + "</body></html></body></html>");
    responseStream.flush();
    responseStream.close();
  }

  @Override
  public void destroy() {
  }
  
  protected static class PortRangeEntry implements Comparable<PortRangeEntry> {

    private Integer portNumber;
    private boolean isAvailable;

    protected static PortRangeEntry createNewAvailable(Integer aNumber) {
      PortRangeEntry created = new PortRangeEntry();
      created.portNumber = aNumber;
      created.isAvailable = true;
      
      return created;
    }

    protected static PortRangeEntry createNewActive(Integer aNumber) {
      PortRangeEntry created = new PortRangeEntry();
      created.portNumber = aNumber;
      created.isAvailable = false;
      
      return created;
    }

    @Override
    public int compareTo(PortRangeEntry o) {
      return portNumber.compareTo(o.portNumber);
    }
  }

}
