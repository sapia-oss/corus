package org.sapia.corus.cluster;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import jersey.repackaged.com.google.common.collect.Lists;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.http.HttpExtensionManager;

public class ClusterHttpExtension implements HttpExtension {

  public static final String HTTP_CLUSTER_CONTEXT = "cluster";

  private final ServerContext serverContext;
  private final ClusterManager manager;

  public ClusterHttpExtension(ServerContext serverContext, ClusterManager manager) {
    this.serverContext = serverContext;
    this.manager = manager;
  }

  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(HTTP_CLUSTER_CONTEXT);
    info.setName("Cluster");
    info.setDescription("View <a href=\"" + HTTP_CLUSTER_CONTEXT + "\"/>cluster</a> status");
    return info;
  }

  @Override
  public void process(HttpContext httpContext) throws Exception {
    ArrayList<CorusHost> allHosts = Lists.newArrayList(manager.getClusterStatus().getHost());
    allHosts.addAll(manager.getHosts());

    outputClusterStatus(allHosts, httpContext);
  }

  @Override
  public void destroy() {
  }

  protected void outputClusterStatus(Collection<CorusHost> hosts, HttpContext httpContext) throws IOException {
    httpContext.getResponse().setHeader("Content-Type", "text/html");
    httpContext.getResponse().setStatusCode(HttpResponseFacade.STATUS_OK);

    PrintStream responseStream = new PrintStream(httpContext.getResponse().getOutputStream());
    responseStream.println("<html><title>Corus Cluster</title><head>" + HttpExtensionManager.STYLE_HEADER + "</head><body>");

    responseStream.println("<h3>Domain</h3>");
    responseStream.println("&nbsp;&nbsp;&nbsp;" + serverContext.getCorus().getDomain());
    responseStream.println("<br/>");
    responseStream.println("<br/>");

    responseStream.println("<h3>Cluster Status</h3>");
    generateClusterStatusTable(hosts, responseStream);
    responseStream.println("<br/>");

    responseStream.println(HttpExtensionManager.FOOTER + "</body></html>");
    responseStream.flush();
    responseStream.close();
  }

  protected void generateClusterStatusTable(Collection<CorusHost> hosts, PrintStream output) {
    output.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"80%\">");
    output.println("<th></th>" +
        "<th width=\"20%\">Node</th>" +
        "<th width=\"20%\">Address</th>" +
        "<th width=\"25%\">JVM</th>" +
        "<th width=\"25%\">OS</th>" +
        "<th width=\"10%\">Repo Role</th>"
        );

    int count = 1;
    for (CorusHost h : hosts) {
      output.println("<tr valign=\"top\">" +
          "<td>" + String.valueOf(count++) + "</td>" +
          "<td>" + h.getHostName() + "</td>" +
          "<td align=\"center\">" + h.getEndpoint().getServerTcpAddress().getHost() + ":" + h.getEndpoint().getServerTcpAddress().getPort() + "</td>" +
          "<td>" + h.getJavaVmInfo() + "</td>" +
          "<td>" + h.getOsInfo() + "</td>" +
          "<td align=\"center\">" + String.valueOf(h.getRepoRole()).toLowerCase() + "</td>" +
          "</tr>"
          );
    }

    output.println("</table>");
  }

}
