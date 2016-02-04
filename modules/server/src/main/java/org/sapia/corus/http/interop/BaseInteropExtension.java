package org.sapia.corus.http.interop;

import java.util.List;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.interop.InteropCodec.InteropWireFormat;
import org.sapia.corus.interop.InteropCodecFactory;
import org.sapia.corus.interop.api.message.ConfirmShutdownMessageCommand;
import org.sapia.corus.interop.api.message.MessageCommand;
import org.sapia.corus.interop.api.message.PollMessageCommand;
import org.sapia.corus.interop.api.message.ProcessMessageHeader;
import org.sapia.corus.interop.api.message.RestartMessageCommand;
import org.sapia.corus.interop.api.message.StatusMessageCommand;
import org.sapia.corus.interop.helpers.RequestListener;
import org.sapia.corus.interop.helpers.ServerStatelessStreamHelper;

/**
 * This extension implement the server side of the Corus interoperability spec.
 * It can be accessed with an URL similar as the following one:
 * <p>
 * 
 * <pre>
 * http://localhost:33000/interoap/soap
 * </pre>
 * 
 * @author Yanick Duchesne
 */
abstract class BaseInteropExtension implements HttpExtension, RequestListener {

  public static final String BASE_INTEROP_CONTEXT = "interop/";

  private InteropWireFormat           iopWireFormat;
  private ServerStatelessStreamHelper helper;
  private Logger                      logger;
  private ServerContext               serverContext;

  BaseInteropExtension(InteropWireFormat wireFormat, ServerContext serverContext) {
    iopWireFormat = wireFormat;
    helper        = new ServerStatelessStreamHelper(InteropCodecFactory.getByType(wireFormat.type()), this, "corus.server");
    logger        = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
    this.serverContext = serverContext;
  }
  
  // --------------------------------------------------------------------------
  // HttpExtension interface

  @Override
  public HttpExtensionInfo getInfo() {
    HttpExtensionInfo info = new HttpExtensionInfo();
    info.setContextPath(BASE_INTEROP_CONTEXT + iopWireFormat.type());
    info.setName(String.format("Corus Interop Link - (%s wire format)", iopWireFormat.type()));
    info.setDescription("Call-back invoked by Corus processes, as described in the Corus Interoperability Specification");
    return info;
  }

  @Override
  public void process(HttpContext ctx) throws Exception {
    ctx.getResponse().setHeader("Content-Type", "text/xml");
    try {
      helper.processRequest(ctx.getRequest().getInputStream(), ctx.getResponse().getOutputStream());
    } catch (Exception e) {
      throw new Exception("Error processing request", e);
    } finally {
      ctx.getResponse().commit();
    }
  }
  
  @Override
  public void destroy() {
  }
  
  // --------------------------------------------------------------------------
  // RequestListener interface

  @Override
  public synchronized void onConfirmShutdown(ProcessMessageHeader header, ConfirmShutdownMessageCommand confirm) throws Exception {
    if (header.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }

    logger.info("Process: " + header.getCorusPid() + " confirming shutdown");
    Processor processor = serverContext.getServices().getProcessor();
    processor.confirmShutdown(header.getCorusPid());
  }

  public synchronized List<MessageCommand> onPoll(ProcessMessageHeader header, PollMessageCommand poll) throws Exception {
    if (header.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Process: " + header.getCorusPid() + " polling...");
    }
    Processor processor = serverContext.getServices().getProcessor();
    Process corusProcess = processor.getProcess(header.getCorusPid());
    List<MessageCommand> commands = corusProcess.poll();
    logger.debug("Process commands: " + commands);
    corusProcess.save();
    return commands;
  }

  public synchronized void onRestart(ProcessMessageHeader header, RestartMessageCommand restart) throws Exception {
    if (header.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Process requested a restart: " + header.getCorusPid());
    }
    Processor processor = serverContext.getServices().getProcessor();
    processor.restart(header.getCorusPid(), KillPreferences.newInstance());
  }

  public synchronized List<MessageCommand> onStatus(ProcessMessageHeader header, StatusMessageCommand status) throws Exception {
    if (header.getCorusPid() == null) {
      throw new MissingDataException("'corusPid' not specified in header");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Status received for " + header.getCorusPid());
    }
    Processor processor = serverContext.getServices().getProcessor();
    Process corusProcess = processor.getProcess(header.getCorusPid());
    List<MessageCommand> commands = corusProcess.status(status);
    logger.debug("Process commands: " + commands);
    corusProcess.save();
    return commands;
  }

}
