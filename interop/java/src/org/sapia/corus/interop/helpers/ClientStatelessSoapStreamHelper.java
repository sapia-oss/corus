package org.sapia.corus.interop.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.sapia.corus.interop.ConfirmShutdown;
import org.sapia.corus.interop.InteropProcessor;
import org.sapia.corus.interop.Poll;
import org.sapia.corus.interop.Process;
import org.sapia.corus.interop.Restart;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.client.CyclicIdGenerator;
import org.sapia.corus.interop.client.InteropProtocol;
import org.sapia.corus.interop.client.Log;
import org.sapia.corus.interop.client.StdoutLog;
import org.sapia.corus.interop.soap.Body;
import org.sapia.corus.interop.soap.Envelope;
import org.sapia.corus.interop.soap.Fault;
import org.sapia.corus.interop.soap.FaultException;
import org.sapia.corus.interop.soap.Header;

import org.sapia.util.xml.ProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;


/**
 * An instance of this class can be reused to implement the client-side
 * behavior of Corus's interoperability protocol. In fact, this class
 * implements the <code>InteropProtocol</code> interface.
 * <p>
 * This class implements stateless, stream-based communication (i.e.: connection closed after
 * request/response cycle). The connection implementations are given to an instance of
 * this class through the <code>newConnection()</code> template method, that must be
 * implemented by inheriting classes.
 * <p>
 * The underlying messaging protocol is of course SOAP.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class ClientStatelessSoapStreamHelper implements InteropProtocol {
  private String           _corusPid;
  private InteropProcessor _soap     = new InteropProcessor();
  protected Log            _log      = new StdoutLog();
  private byte[]           _buf      = new byte[2048];

  protected ClientStatelessSoapStreamHelper(String corusPid) {
    _corusPid = corusPid;
  }

  /**
   * @see InteropProtocol#setLog(Log)
   */
  public void setLog(Log log) {
    _log = log;
  }

  /**
   * This template method is called by an instance of this class
   * every time it needs to connect to its remote endpoint.
   *
   * @return a <code>StreamConnection</code>.
   * @throws IOException
   */
  protected abstract StreamConnection newStreamConnection()
                                                   throws IOException;

  protected void doSendConfirmShutdown() throws FaultException, IOException {
    ConfirmShutdown confirm = new ConfirmShutdown();
    confirm.setCommandId(CyclicIdGenerator.newCommandId());
    doSendRequest(new Object[] { confirm });
  }

  protected List doSendPoll() throws FaultException, IOException {
    Poll poll = new Poll();
    poll.setCommandId(CyclicIdGenerator.newCommandId());

    return doSendRequest(new Object[] { poll });
  }

  protected void doSendRestart() throws FaultException, IOException {
    Restart res = new Restart();
    res.setCommandId(CyclicIdGenerator.newCommandId());
    doSendRequest(new Object[] { res });
  }

  protected List doSendStatus(Status stat, boolean isPoll) throws FaultException, IOException {
    if (isPoll) {
      Poll poll = new Poll();
      poll.setCommandId(CyclicIdGenerator.newCommandId());
      stat.setCommandId(CyclicIdGenerator.newCommandId());
      return doSendRequest(new Object[] { poll, stat } );

    } else {
      stat.setCommandId(CyclicIdGenerator.newCommandId());
      return doSendRequest(new Object[] { stat } );
    }
  }

  protected synchronized List doSendRequest(Object[] toSend)
                        throws FaultException, IOException {
    Envelope env = createSoapRequest();
    for (int i = 0; i < toSend.length; i++) {
      _log.debug("Sending request to corus: " + toSend[i]);
      env.getBody().addObject(toSend[i]);
    }

    StreamConnection conn         = newStreamConnection();
    OutputStream     os           = null;
    InputStream      is           = null;
    boolean          outputClosed = false;

    try {
      try {
        os = conn.getOutputStream();
        _soap.serialize(env, os);
        os.flush();
        os.close();
        outputClosed = true;
        is           = conn.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int read = 0;
        while((read = is.read(_buf, 0, _buf.length)) > 0){
          bos.write(_buf, 0, read); 
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        Object response = _soap.deserialize(bis);
        env = (Envelope) response;

        List toReturn = env.getBody().getObjects();

        if (toReturn.get(0) instanceof Fault) {
          throw new FaultException((Fault) toReturn.get(0));
        }

        return toReturn;
      } catch (ProcessingException e) {
        _log.fatal("Error performing XML/Object transformations", e);
        throw new IOException(e.getMessage());
      }
    } finally {
      if ((os != null) && !outputClosed) {
        os.close();
      }

      if (is != null) {
        is.close();
      }
    }
  }

  private Envelope createSoapRequest() {
    Process proc = new Process();
    proc.setCorusPid(_corusPid);
    proc.setRequestId(CyclicIdGenerator.newRequestId());

    Header head = new Header();
    head.addObject(proc);

    Envelope env = new Envelope();
    env.setHeader(head);
    env.setBody(new Body());

    return env;
  }
}
