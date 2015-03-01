package org.sapia.corus.http.helpers;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Generates HTML content for the home page.
 * 
 * @author yduchesne
 * 
 */
public class HomePageHelper {

  private List<HttpExtensionInfo> extensionInfos = new ArrayList<HttpExtensionInfo>();
  private ServerContext context;

  public HomePageHelper(ServerContext context, Collection<HttpExtensionInfo> extInfos) {
    extensionInfos.addAll(extInfos);
    this.context = context;
  }

  public void print(Request req, Response res) throws Exception {
    try {
      res.setValue("Content-Type", "text/html");
      res.setCode(HttpResponseFacade.STATUS_OK);
      PrintStream ps = res.getPrintStream();
      ps.println("<html><body><h1>Welcome to Corus @ " + context.getDomain() + " - version " + CorusVersion.create().toString() + " </h1>");
      
      ps.println("<h2>Available Extensions</h2>");
      ps.println("<table border=\"0\">");
      ps.println("<th>Context Path</th><th>Name</th><th>Description</th>");
      Collections.sort(extensionInfos);
      for (int i = 0; i < extensionInfos.size(); i++) {
        ps.print("<tr>");
        HttpExtensionInfo info = (HttpExtensionInfo) extensionInfos.get(i);

        ps.print("<td>");
        ps.print(info.getContextPath());
        ps.print("</td>");

        ps.print("<td><b>");
        ps.print(info.getName());
        ps.print("</b></td>");

        ps.print("<td>");
        if (info.getDescription() != null) {
          ps.print(info.getDescription());
          ps.print(".");
        }
        ps.print("</td>");

        ps.println("</tr>");
      }
      ps.println("</table>");
      ps.println(HttpExtensionManager.FOOTER);
      ps.println("</body></html>");
      ps.flush();
      ps.close();
      res.commit();
    } catch (IOException e) {
      try {
        res.getOutputStream().close();
        res.commit();
      } catch (IOException e2) {
      }
    }
  }

}
