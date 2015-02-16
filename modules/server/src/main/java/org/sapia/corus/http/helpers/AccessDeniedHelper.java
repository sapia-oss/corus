package org.sapia.corus.http.helpers;

import java.io.PrintStream;

import org.sapia.corus.client.services.http.HttpRequestFacade;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.http.HttpExtensionManager;
import org.sapia.ubik.util.Streams;

/**
 * Generates HTML content for the 403 error page.
 * 
 * @author yduchesne
 * 
 */
public class AccessDeniedHelper implements OutputHelper {

  public void print(HttpRequestFacade req, HttpResponseFacade res) throws Exception {
    res.setHeader("Content-Type", "text/html");
    res.setStatusCode(HttpResponseFacade.STATUS_ACCESS_DENIED);
    PrintStream ps = new PrintStream(res.getOutputStream());
    ps.println("<html><body><h1>403 - ACCESS DENIED</h1> " + HttpExtensionManager.FOOTER + "</body></html>");
    Streams.flushAndCloseSilently(ps);
  }
}
