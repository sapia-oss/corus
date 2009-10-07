package org.sapia.corus.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.ArgFactory;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.http.HttpContext;
import org.sapia.corus.http.HttpExtension;
import org.sapia.corus.http.HttpExtensionInfo;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;
import org.sapia.ubik.net.TCPAddress;

import simple.http.Request;

public class ProcessorExtension implements HttpExtension{
  
  public static final String CONTEXT_PATH = "processor";
  
  private static final String COMMAND_PS = "/ps";
  
  private static final String COMMAND_STATUS = "/status";
  
  private static final String PARAM_DIST = "d";
  private static final String PARAM_VERSION = "v";
  private static final String PARAM_PROC = "n";
  private static final String PARAM_PROFILE = "p";
  private static final String PARAM_ID = "i";
  
  private Processor _processor;
  
  ProcessorExtension(Processor proc){
    _processor = proc;
  }
  
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(CONTEXT_PATH);
    info.setName("Processor");
    info.setDescription("Allows viewing currently <a href=\""+ CONTEXT_PATH + "/ps\"/>running processes</a> (/ps) and their "+
        "<a href=\""+ CONTEXT_PATH + "/status\"/>status</a> (/status) - takes d, v, p, n query string parameters");
    return info;
  }
  
  public void process(HttpContext ctx) throws Exception {
    if(ctx.getPathInfo().startsWith(COMMAND_PS)){
      processPs(ctx);
    }
    else if(ctx.getPathInfo().startsWith(COMMAND_STATUS)){
      processStatus(ctx);      
    }
    else{
      throw new FileNotFoundException(ctx.getPathInfo());
    }
  }
  
  private void processPs(HttpContext ctx) throws IOException{
    outputProcesses(ctx, filterProcesses(ctx.getRequest()), false);
  }
  
  private void processStatus(HttpContext ctx) throws IOException{
    outputProcesses(ctx, filterProcesses(ctx.getRequest()), true);
  }  
  
  private Arg arg(String name, Request r) throws IOException{
    String value = r.getParameter(name);
    if(value != null){
      return ArgFactory.parse(value);
    }
    return null;
  }
  
  private void outputProcesses(HttpContext ctx, List processes, boolean status) throws IOException{
    ctx.getResponse().set("Content-Type", "text/xml");    
    PrintStream ps = ctx.getResponse().getPrintStream();
    ps.print("<processes ");
    attribute("domain", CorusRuntime.getCorus().getDomain(), ps);
    try{
      TCPAddress addr = (TCPAddress)CorusRuntime.getTransport().getServerAddress();
      attribute("host", addr.getHost(), ps);      
      attribute("port", Integer.toString(addr.getPort()), ps);
    }catch(ClassCastException e){}
    ps.println(">");
    for(int i = 0; i < processes.size(); i++){
      Process proc = (Process)processes.get(i);
      ps.println("  <process ");
      attribute("id", proc.getProcessID(), ps);
      ps.println();
      attribute("osPid", proc.getOsPid(), ps);
      ps.println();      
      attribute("creationTime", Long.toString(proc.getCreationTime()), ps);
      ps.println();      
      attribute("creationDate", new Date(proc.getCreationTime()), ps);
      ps.println();      
      attribute("name", proc.getDistributionInfo().getProcessName(), ps);
      ps.println();      
      attribute("dist", proc.getDistributionInfo().getName(), ps);
      ps.println();
      attribute("version", proc.getDistributionInfo().getVersion(), ps);
      ps.println();
      attribute("profile", proc.getDistributionInfo().getProfile(), ps);
      if(status){
        ps.println(">");
        ps.println();
        Status stat = proc.getProcessStatus();
        ps.println("    <status>");
        if(stat != null){        
          List contexts = stat.getContexts();
          for(int j = 0; j < contexts.size(); j++){
            Context statusCtx = (Context)contexts.get(j);
            ps.print("      <context name=\"");
            ps.print(statusCtx.getName());
            ps.println("\">");
            List params = statusCtx.getParams();
            for(int k = 0; k < params.size(); k++){
              Param p = (Param)params.get(k);

              if (containsIllegalXmlCharacter(p.getValue())) {
                ps.print("        <param ");
                attribute(p.getName(), "![CDATA]", ps);
                ps.println(" >\n<![CDATA[");
                ps.println(p.getValue());
                ps.println("]]>");
                ps.println("        </param>");
                
              } else {
                ps.print("        <param ");
                attribute(p.getName(), p.getValue(), ps);
                ps.println(" />");
              }
            }
            ps.println("      </context>");
          }
        }
        ps.println("    </status>");
        ps.println("  </process>");
        ps.flush();
      }
      else{
        ps.println("/>");
      }
    }
    ps.println("</processes>");
    ps.flush();
    ps.close();
  }
  
  private List filterProcesses(Request req) throws IOException{
    Arg d = arg(PARAM_DIST, req);
    Arg v = arg(PARAM_VERSION, req);    
    Arg n = arg(PARAM_PROC, req);
    String i = req.getParameter(PARAM_ID);
    String p = req.getParameter(PARAM_PROFILE);
    
    List processes;
    if(i != null){
      try{
        Process proc = _processor.getProcess(i);
        processes = new ArrayList(1);
        processes.add(proc);
      }catch(LogicException e){
        processes = Collections.EMPTY_LIST;
      }
    }
    else if(p != null && d != null && v != null && n != null){
      processes = _processor.getProcesses(d, v, p, n);
    }
    else if(p != null && d != null && v != null){
      processes = _processor.getProcesses(d, v, p);
    }
    else if(d != null && v != null){
      processes = _processor.getProcesses(d, v);
    }
    else if(d != null){
      processes = _processor.getProcesses(d);
    }
    else{
      processes = _processor.getProcesses();
    }
    return processes;
  }
  
  private void attribute(String name, Object value, PrintStream ps){
    ps.print("    ");
    ps.print(name);
    ps.print("=\"");
    ps.print(value);
    ps.print("\"");
  }
  
  protected boolean containsIllegalXmlCharacter(String aValue) {
    return aValue != null &&
             (aValue.contains("<") ||
              aValue.contains(">") ||
              aValue.contains("&") ||
              aValue.contains("'") ||
              aValue.contains("\""));
  }
}
