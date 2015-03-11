package org.sapia.corus.deployer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpRequestFacade;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.TCPAddress;

public class DeployerExtension implements HttpExtension {

  public static final String CONTEXT_PATH = "deployer";
  private static final String COMMAND_LS = "/ls";
  private static final String PARAM_DIST = "d";
  private static final String PARAM_VERSION = "v";

  private Deployer deployer;
  private ServerContext context;

  DeployerExtension(Deployer deployer, ServerContext context) {
    this.deployer = deployer;
    this.context = context;
  }

  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Deployer");
    info.setDescription("Allows <a href=\"" + CONTEXT_PATH + "/ls\"/>viewing</a> deployed distributions (/ls) - takes d, v query string parameters");
    return info;
  }

  public void process(HttpContext ctx) throws Exception {
    if (ctx.getPathInfo().startsWith(COMMAND_LS)) {
      processLs(ctx);
    } else {
      throw new FileNotFoundException(ctx.getPathInfo());
    }
  }

  private void processLs(HttpContext ctx) throws IOException {
    outputDists(ctx, filterDists(ctx.getRequest()), false);
  }

  private ArgMatcher arg(String name, HttpRequestFacade r) throws IOException {
    try {
      String value = r.getParameter(name);
      if (value != null) {
        return ArgMatchers.parse(value);
      }
    } catch (Exception e) {
      // noop
    }
    return null;
  }

  private void outputDists(HttpContext ctx, List<Distribution> dists, boolean status) throws IOException {
    ctx.getResponse().setHeader("Content-Type", "text/xml");
    PrintStream ps = null;
    try {
      ps = new PrintStream(ctx.getResponse().getOutputStream());
    } catch (Exception e) {
      throw new IOException("Error caught while processing request", e);
    }
    ps.println("<distributions");
    attribute("domain", context.getDomain(), ps);
    try {
      TCPAddress addr = context.getCorusHost().getEndpoint().getServerTcpAddress();
      attribute("host", addr.getHost(), ps);
      attribute("port", Integer.toString(addr.getPort()), ps);
    } catch (ClassCastException e) {
    }
    ps.println(">");
    for (int i = 0; i < dists.size(); i++) {
      Distribution dist = (Distribution) dists.get(i);
      ps.println("  <distribution ");
      attribute("name", dist.getName(), ps);
      ps.println();
      attribute("version", dist.getVersion(), ps);
      ps.println(">");
      ps.println("    <processConfigs>");
      List<ProcessConfig> procs = dist.getProcesses();
      for (int j = 0; j < procs.size(); j++) {
        ProcessConfig proc = (ProcessConfig) procs.get(j);
        ps.print("      <processConfig ");
        attribute("name", proc.getName(), ps);
        attribute("maxKillRetry", Integer.toString(proc.getMaxKillRetry()), ps);
        attribute("pollInterval", Integer.toString(proc.getPollInterval()), ps);
        attribute("statusInterval", Integer.toString(proc.getStatusInterval()), ps);
        attribute("shutDownTimeout", Integer.toString(proc.getShutdownTimeout()), ps);
        attribute("deleteOnKill", Boolean.toString(proc.isDeleteOnKill()), ps);
        attribute("invoker", Boolean.toString(proc.isInvoke()), ps);
        attribute("profiles", proc.getProfiles(), ps);
        ps.println("/>");
      }
      ps.println("    </processConfigs>");
      ps.println("  </distribution>");
      ps.flush();
    }
    ps.println("</distributions>");
    ps.flush();
    ps.close();
  }

  private List<Distribution> filterDists(HttpRequestFacade req) throws IOException {
    ArgMatcher d = arg(PARAM_DIST, req);
    ArgMatcher v = arg(PARAM_VERSION, req);

    DistributionCriteria criteria = DistributionCriteria.builder().name(d).version(v).build();
    return deployer.getDistributions(criteria);
  }

  private void attribute(String name, Object value, PrintStream ps) {
    ps.print(" ");
    ps.print(name);
    ps.print("=\"");
    ps.print(value);
    ps.print("\"");
  }
}
