package org.sapia.corus.deployer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.ArgFactory;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.http.HttpContext;
import org.sapia.corus.http.HttpExtension;
import org.sapia.corus.http.HttpExtensionInfo;
import org.sapia.ubik.net.TCPAddress;

import simple.http.Request;

public class DeployerExtension implements HttpExtension{
  
  public static final String CONTEXT_PATH = "deployer";
  
  private static final String COMMAND_LS = "/ls";
  
  private static final String PARAM_DIST = "d";
  private static final String PARAM_VERSION = "v";
  
  private Deployer _deployer;
  
  DeployerExtension(Deployer dep){
    _deployer = dep;
  }
  
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Deployer");
    info.setDescription("Allows <a href=\""+ CONTEXT_PATH + "/ls\"/>viewing</a> deployed distributions (/ls) - takes d, v query string parameters");
    return info;
  }
  
  public void process(HttpContext ctx) throws Exception {
    if(ctx.getPathInfo().startsWith(COMMAND_LS)){
      processLs(ctx);
    }
    else{
      throw new FileNotFoundException(ctx.getPathInfo());
    }
  }
  
  private void processLs(HttpContext ctx) throws IOException{
    outputDists(ctx, filterDists(ctx.getRequest()), false);
  }
  
  private Arg arg(String name, Request r) throws IOException{
    String value = r.getParameter(name);
    if(value != null){
      return ArgFactory.parse(value);
    }
    return null;
  }
  
  private void outputDists(HttpContext ctx, List processes, boolean status) throws IOException{
    ctx.getResponse().set("Content-Type", "text/xml");    
    PrintStream ps = ctx.getResponse().getPrintStream();
    ps.println("<distributions");
    attribute("domain", CorusRuntime.getCorus().getDomain(), ps);
    try{
      TCPAddress addr = (TCPAddress)CorusRuntime.getTransport().getServerAddress();
      attribute("host", addr.getHost(), ps);      
      attribute("port", Integer.toString(addr.getPort()), ps);
    }catch(ClassCastException e){}
    ps.println(">");    
    for(int i = 0; i < processes.size(); i++){
      Distribution dist = (Distribution)processes.get(i);
      ps.println("  <distribution ");
      attribute("name", dist.getName(), ps);
      ps.println();
      attribute("version", dist.getVersion(), ps);
      ps.println(">");
      ps.println("    <processConfigs>");
      List procs = dist.getProcesses();
      for(int j = 0; j < procs.size(); j++){
        ProcessConfig proc = (ProcessConfig)procs.get(j);
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
  
  private List filterDists(Request req) throws IOException{
    Arg d = arg(PARAM_DIST, req);
    Arg v = arg(PARAM_VERSION, req);    
    
    List dists;
    if(d != null && v != null){
      dists = _deployer.getDistributions(d, v);
    }
    else if(d != null){
      dists = _deployer.getDistributions(d);
    }
    else{
      dists = _deployer.getDistributions();
    }
    return dists;
  }
  
  private void attribute(String name, Object value, PrintStream ps){
    ps.print(" ");
    ps.print(name);
    ps.print("=\"");
    ps.print(value);
    ps.print("\"");
  }  
}
