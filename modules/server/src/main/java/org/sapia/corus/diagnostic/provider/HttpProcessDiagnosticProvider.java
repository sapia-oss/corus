package org.sapia.corus.diagnostic.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.deployer.dist.HttpDiagnosticConfig;
import org.sapia.corus.client.services.deployer.dist.HttpsDiagnosticConfig;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.diagnostic.DiagnosticContext;
import org.sapia.corus.diagnostic.ProcessDiagnosticProvider;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.SysClock;

/**
 * Performs diagnostic through connecting to processes over HTTP/HTTPS.
 * 
 * @author yduchesne
 *
 */
public class HttpProcessDiagnosticProvider implements ProcessDiagnosticProvider {
  
  private static final String ROOT_PATH = "/";
  
  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getSimpleName());
  
  private SysClock clock = SysClock.RealtimeClock.getInstance();
  
  private static final Set<String> ACCEPTED_PROTOCOLS = Collects.arrayToSet(
      HttpDiagnosticConfig.PROTOCOL_HTTP, HttpsDiagnosticConfig.PROTOCOL_HTTPS
  );
  
  void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  @Override
  public boolean accepts(DiagnosticContext context) {
    return ACCEPTED_PROTOCOLS.contains(context.getDiagnosticConfig().getProtocol()) && context.getDiagnosticConfig() instanceof HttpDiagnosticConfig;
  }
  
  @Override
  public ProcessDiagnosticResult performDiagnostic(DiagnosticContext context) {
    HttpURLConnection conn        = null;
    InputStream       is          = null;
    HttpDiagnosticConfig    conf        = (HttpDiagnosticConfig) context.getDiagnosticConfig();
    String            connString  = createConnectionString(context);
    if (log.isDebugEnabled()) log.debug(String.format("Connecting to %s for process: %s", connString, ToStringUtil.toString(context.getProcess())));
    try {
      conn = (HttpURLConnection) new URL(connString).openConnection();
      conn.setRequestMethod("GET");
      conn.setDoInput(true);
      conn.setDoOutput(false);
      conn.setUseCaches(false);
      conn.setConnectTimeout(conf.getConnectionTimeout());
      conn.setReadTimeout(conf.getReadTimeout());
      is = conn.getInputStream();
      int statusCode = conn.getResponseCode();
      if (statusCode != conf.getSuccessCode()) {
        return new ProcessDiagnosticResult(
            ProcessDiagnosticStatus.CHECK_FAILED, 
            conn.getResponseMessage() == null ? 
                String.format("HTTP error occured (status code: %s)", statusCode) 
                : String.format("%s (%s)",  conn.getResponseMessage(), statusCode) , 
            context.getProcess(), context.getDiagnosticConfig().getProtocol(), context.getPort());
      }
      return new ProcessDiagnosticResult(
          ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "Check succesful on: " + connString, 
          context.getProcess(), context.getDiagnosticConfig().getProtocol(), context.getPort()
      );
    } catch (IOException e) {
      if (clock.currentTimeMillis() - context.getProcess().getCreationTime() < 
          TimeUnit.SECONDS.toMillis(context.getDiagnosticConfig().getGracePeriod())) {
        log.error("Connection issue trying to acquire diagnostic for process: " + ToStringUtil.toString(context.getProcess()) 
            + ". Still within grace period, will flag as suspect", e);
        return new ProcessDiagnosticResult(
            ProcessDiagnosticStatus.SUSPECT, e.getMessage(), context.getProcess(), context.getDiagnosticConfig().getProtocol(), context.getPort()
        );
      } else {
        log.error("Could not acquire diagnostic for process: " + ToStringUtil.toString(context.getProcess()), e);
        return new ProcessDiagnosticResult(
            ProcessDiagnosticStatus.CHECK_FAILED, e.getMessage(), context.getProcess(), context.getDiagnosticConfig().getProtocol(), context.getPort()
        );
      }
    } finally {
      if (conn!= null) {
        conn.disconnect();
      }
      if (is != null) {
        IOUtils.closeQuietly(is);
      }
    }
  }
  
  private String createConnectionString(DiagnosticContext context) {
    HttpDiagnosticConfig httpDg = (HttpDiagnosticConfig) context.getDiagnosticConfig();
    int port = httpDg.getPortPrefix() > 0 ? Integer.parseInt("" + httpDg.getPortPrefix() + context.getPort().getPort()) : context.getPort().getPort();
    return httpDg.getProtocol() + "://"
        + context.getServerContext().getCorusHost().getEndpoint().getServerTcpAddress().getHost() 
        + ":" + port
        + (httpDg.getPath().startsWith(ROOT_PATH) ? httpDg.getPath() : ROOT_PATH + httpDg.getPath());

  }
}
