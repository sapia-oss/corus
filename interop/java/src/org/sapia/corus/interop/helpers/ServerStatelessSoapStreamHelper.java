package org.sapia.corus.interop.helpers;

import org.sapia.corus.interop.Ack;
import org.sapia.corus.interop.ConfirmShutdown;
import org.sapia.corus.interop.InteropProcessor;
import org.sapia.corus.interop.Poll;
import org.sapia.corus.interop.Process;
import org.sapia.corus.interop.Restart;
import org.sapia.corus.interop.Server;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.client.CyclicIdGenerator;
import org.sapia.corus.interop.soap.Body;
import org.sapia.corus.interop.soap.Envelope;
import org.sapia.corus.interop.soap.Fault;
import org.sapia.corus.interop.soap.Header;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class can be reused in servers that receive requests from Corus clients.
 * This class implements the Corus interoperability protocol through stateless,
 * stream SOAP request handling. This class can be used in conjonction with the
 * <code>ClientStatelessSoapStreamHelper</code> class in client-server implementations.
 * <p>
 * An instance of this class is given a <code>RequestListener</code> instance at construction
 * time; the listener is in charge of handling the incoming commands.
 * <p>
 * Once an instance of this class is created, server implementations need only calling
 * its <code>process()</code> method, passing to the latter the expected <code>InputStream</code>
 * and <code>OutputStream</code>.
 *
 * @see org.sapia.corus.interop.helpers.RequestListener
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerStatelessSoapStreamHelper {
  private RequestListener  _listener;
  private InteropProcessor _proc  = new InteropProcessor();
  private String           _actor;

  /**
   * Creates an instance of this class.
   *
   * @param listener a <code>RequestListener</code>
   * @param actor a <code>String</code> that is used when sending back
   * SOAP faults to client; SOAP faults can indeed contain "actor" information
   * to help identify the source of an error.
   */
  public ServerStatelessSoapStreamHelper(RequestListener listener, String actor) {
    _listener = listener;
    _actor    = actor;
  }

  /**
   * Processes the given request stream, returning the response in the given output stream.
   *
   * @param request an <code>InputStream</code> expected to contain XML that complies with
   * the Corus interop spec.
   * @param response the <code>OutputStream</code> that will be used by this instance
   * to send the response back to the client.
   * @throws Exception if a problem occurs while processing the request, and if no SOAP
   * fault could be sent back to the client.
   */
  public void processRequest(InputStream request, OutputStream response)
                      throws Exception {
    long start = System.currentTimeMillis();

    try {
      Envelope env = (Envelope) _proc.deserialize(request);

      if ((env.getHeader().getObjects().size() == 0) ||
            !(env.getHeader().getObjects().get(0) instanceof Process)) {
        sendFault(response,
                  new Exception("'Process' element missing in header"),
                  System.currentTimeMillis() - start);

        return;
      }

      Process proc = (Process) env.getHeader().getObjects().get(0);

      if (proc.getCorusPid() == null) {
        sendFault(response,
                  new Exception("'corusPid' missing in 'Process' element of request header"),
                  System.currentTimeMillis() - start);

        return;
      }

      if (proc.getRequestId() == null) {
        sendFault(response,
                  new Exception("'requestId' missing in 'Process' element of request header"),
                  System.currentTimeMillis() - start);

        return;
      }

      if (env.getBody().getObjects().size() == 0) {
        Ack ack = new Ack();
        ack.setCommandId(CyclicIdGenerator.newCommandId());
        sendResponse(response, ack, System.currentTimeMillis() - start);

        return;
      }

      // Process the commands
      List commands = new ArrayList();
      for (Iterator it = env.getBody().getObjects().iterator(); it.hasNext(); ) {
        Object command = it.next();

        if (command instanceof Poll) {
          if (command instanceof Poll) {
            List pollCommands = _listener.onPoll(proc, (Poll) command);
            commands.addAll(pollCommands);
          }

        } else if (command instanceof Status) {
          if (command instanceof Status) {
            List statusCommands = _listener.onStatus(proc, (Status) command);
            commands.addAll(statusCommands);
          }

        } else if (command instanceof ConfirmShutdown) {
          _listener.onConfirmShutdown(proc, (ConfirmShutdown) command);

        } else if (command instanceof Restart) {
          _listener.onRestart(proc, (Restart) command);
        }
      }

      // Send the result
      if (commands.size() > 0) {
        sendResponse(response, commands, System.currentTimeMillis() - start);
      } else {
        sendAck(response, System.currentTimeMillis() - start);
      }
    } catch (Exception e) {
      sendFault(response, e, System.currentTimeMillis() - start);
    } finally {
      request.close();
      response.close();
    }
  }

  protected void sendFault(OutputStream out, Exception e, long processingTime)
                    throws Exception {
    Fault                 f   = new Fault();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter           pw  = new PrintWriter(bos);
    e.printStackTrace(pw);
    pw.flush();
    pw.close();
    f.setDetail(new String(bos.toByteArray()));
    f.setFaultactor(_actor);
    f.setFaultstring(e.getMessage());
    f.setFaultcode("500");
    sendResponse(out, f, processingTime);
  }

  protected void sendAck(OutputStream out, long processingTime)
                  throws Exception {
    Ack ack = new Ack();
    ack.setCommandId(CyclicIdGenerator.newCommandId());
    sendResponse(out, ack, processingTime);
  }

  protected void sendResponse(OutputStream out, Object toSend,
                              long processingTime) throws Exception {
    Envelope env = createSoapResponse(processingTime);

    if (toSend instanceof Collection) {
      Iterator itr = ((Collection) toSend).iterator();

      while (itr.hasNext()) {
        env.getBody().addObject(itr.next());
      }
    } else {
      env.getBody().addObject(toSend);
    }

    _proc.serialize(env, out);
  }

  private Envelope createSoapResponse(long processingTime) {
    Server svr = new Server();
    svr.setRequestId(CyclicIdGenerator.newRequestId());
    svr.setProcessingTime(processingTime);

    Header header = new Header();
    header.addObject(svr);

    Envelope env = new Envelope();
    env.setHeader(header);
    env.setBody(new Body());

    return env;
  }
}
