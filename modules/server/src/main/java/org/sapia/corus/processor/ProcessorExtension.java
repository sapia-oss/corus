package org.sapia.corus.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ServerContext;
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
  private ServerContext _context;
  
  ProcessorExtension(Processor proc, ServerContext context){
    _processor = proc;
    _context = context;
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
  
  private void processPs(HttpContext ctx) throws IOException, Exception{
    outputProcesses(ctx, filterProcesses(ctx.getRequest()), false);
  }
  
  private void processStatus(HttpContext ctx) throws IOException, Exception{
    outputProcesses(ctx, filterProcesses(ctx.getRequest()), true);
  }  
  
  private Arg arg(String name, Request r) throws IOException, Exception{
    String value = r.getParameter(name);
    if(value != null){
      return ArgFactory.parse(value);
    }
    return null;
  }
  
  private void outputProcesses(HttpContext ctx, List<Process> processes, boolean status) throws IOException, Exception{
    ctx.getResponse().set("Content-Type", "text/xml");    
    PrintStream ps = ctx.getResponse().getPrintStream();
    ps.print("<processes ");
    attribute("domain", _context.getDomain(), ps);
    try{
      TCPAddress addr = _context.getServerAddress();
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
          List<Context> contexts = stat.getContexts();
          for(int j = 0; j < contexts.size(); j++){
            Context statusCtx = (Context)contexts.get(j);
            ps.print("      <context name=\"");
            ps.print(statusCtx.getName());
            ps.println("\">");
            List<Param> params = statusCtx.getParams();
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
  
  private List<Process> filterProcesses(Request req) throws IOException, Exception{
    Arg d = arg(PARAM_DIST, req);
    Arg v = arg(PARAM_VERSION, req);    
    Arg n = arg(PARAM_PROC, req);
    String i = req.getParameter(PARAM_ID);
    String p = req.getParameter(PARAM_PROFILE);
    
    List<Process> processes;
    if(i != null){
      try{
        Process proc = _processor.getProcess(i);
        processes = new ArrayList<Process>(1);
        processes.add(proc);
      }catch(ProcessNotFoundException e){
        processes = new ArrayList<Process>(0);
      }
    }
    else{
      ProcessCriteria criteria = ProcessCriteria.builder()
        .distribution(d)
        .version(v)
        .name(n)
        .profile(p)
        .build();
      processes = _processor.getProcesses(criteria);
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
