package org.sapia.corus.http.interop;

import java.util.List;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.interop.AbstractCommand;
import org.sapia.corus.interop.ConfirmShutdown;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Poll;
import org.sapia.corus.interop.Process;
import org.sapia.corus.interop.Restart;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.helpers.RequestListener;
import org.sapia.corus.interop.helpers.ServerStatelessSoapStreamHelper;


/**
 * This extension implement the server side of the Corus interoperability spec. 
 * It can be accessed with an URL similar as the following one:
 * <p>
 * <pre>http://localhost:33000/interoap/soap</pre>
 * 
 * @author Yanick Duchesne
 */
public class SoapExtension implements HttpExtension, RequestListener {
  
  public static final String HTTP_INTEROP_SOAP_CONTEXT = "interop/soap";  
  
  private ServerStatelessSoapStreamHelper helper;
  private Logger                          logger;
  private ServerContext                   serverContext;
  
  public SoapExtension(ServerContext serverContext){
    helper = new ServerStatelessSoapStreamHelper(this, "corus.server");
    logger = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    this.serverContext = serverContext;
  }
  
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(HTTP_INTEROP_SOAP_CONTEXT);
    info.setName("Corus Interop Link");
    info.setDescription("Call-back invoked by Corus processes, as described in the Corus Interoperability Specification");
    return info;
  }
  
  public void process(HttpContext ctx) throws Exception {
    ctx.getResponse().set("Content-Type", "text/xml");
    try {
      helper.processRequest(ctx.getRequest().getInputStream(), ctx.getResponse().getOutputStream());
    } catch (Exception e) {
      throw new Exception("Error processing request", e);
    }finally{
      ctx.getResponse().commit();
    }
  }
  
  public synchronized void onConfirmShutdown(Process proc,
                                             ConfirmShutdown confirm)
    throws Exception {
    if (proc.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }
    
    logger.info("Process: " + proc + " confirming shutdown");
    org.sapia.corus.client.services.processor.Process corusProcess = serverContext.lookup(Processor.class).getProcess(proc.getCorusPid());
    corusProcess.confirmKilled();
    corusProcess.save();
  }
  
  public synchronized List<AbstractCommand> onPoll(Process proc, Poll poll)
  throws Exception {
    if (proc.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }
  
    if(logger.isDebugEnabled())
      logger.debug("Process: " + proc + " polling...");
    
    org.sapia.corus.client.services.processor.Process corusProcess = serverContext
      .lookup(Processor.class).getProcess(proc.getCorusPid());
    List<AbstractCommand> commands = corusProcess.poll();
    logger.debug("Process commands: " + commands);
    corusProcess.save();
    return commands;
  }
  
  public synchronized void onRestart(Process proc, Restart restart)
  throws Exception {
    if (proc.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }

    if(logger.isDebugEnabled())
      logger.debug("Process requested a restart: " + proc);
    
    serverContext.lookup(Processor.class).restart(proc.getCorusPid());
  }
  
  public synchronized List<AbstractCommand> onStatus(Process proc, Status stat)
  throws Exception {
    if (proc.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }
    
    if(logger.isDebugEnabled()){
      logger.debug("Status received for " + proc);
      for(Context ctx: stat.getContexts()){
        logger.debug("Context: " + ctx.getName());
        for(Param p: ctx.getParams()){
          logger.debug(String.format("   %s = %s", p.getName(), p.getValue()));
        }
      }
    }
    
    org.sapia.corus.client.services.processor.Process corusProcess = serverContext.lookup(Processor.class).getProcess(proc.getCorusPid());
    List<AbstractCommand> commands = corusProcess.status(stat);
    logger.debug("Process commands: " + commands);
    corusProcess.save();
    return commands;
  }
  
}
