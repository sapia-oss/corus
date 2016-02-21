package org.sapia.corus.diagnostic;

import org.sapia.corus.client.services.deployer.dist.DiagnosticConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.core.ServerContext;

public class DiagnosticContext {
  
  private Process       process;
  private ActivePort    activePort;
  private DiagnosticConfig    diagnosticConfig;
  private ServerContext serverContext;
  
  public DiagnosticContext(Process p, ActivePort activePort, DiagnosticConfig config, ServerContext serverContext) {
    this.process = p;
    this.activePort = activePort;
    this.diagnosticConfig = config;
    this.serverContext = serverContext;
  }
  
  public ActivePort getPort() {
    return activePort;
  }
  
  public Process getProcess() {
    return process;
  }
  
  public DiagnosticConfig getDiagnosticConfig() {
    return diagnosticConfig;
  }
  
  public ServerContext getServerContext() {
    return serverContext;
  }
}