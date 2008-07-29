package org.sapia.corus.interop.http;

import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.client.InteropClient;
import org.sapia.corus.interop.helpers.ClientStatelessSoapStreamHelper;
import org.sapia.corus.interop.helpers.StreamConnection;
import org.sapia.corus.interop.soap.FaultException;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements the <code>InteropProtocol</code> interface over HTTP. An instance of
 * this class expects the following system properties to be set upon instantiation:
 *
 * <ul>
 *  <li>corus.server.port
 *  <li>corus.server.host
 * </ul>
 *
 * <p>
 * A corus server is expected to be available at:
 * <p>
 * <code>http://<corus.server.host>:<corus.server.port>/interop/soap</code>
 *
 * @author Yanick Duchesne
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpProtocol extends ClientStatelessSoapStreamHelper {
  private static final List EMPTY_LST  = new ArrayList();
  private URL               _corusUrl;

  public HttpProtocol() throws MalformedURLException {
    super(InteropClient.getInstance().getCorusPid());

    if (InteropClient.getInstance().isDynamic()) {
      int    port = InteropClient.getInstance().getCorusPort();
      String host = InteropClient.getInstance().getCorusHost();

      if (port == InteropClient.UNDEFINED_PORT) {
        throw new IllegalStateException("corus.server.port system property not specified");
      } else if (host == null) {
        throw new IllegalStateException("corus.server.host system property not specified");
      }

      _corusUrl = new URL("http://" + host + ":" + port + "/interop/soap");
      _log.warn("HTTP protocol activated; corus server endpoint set to:  " +
                _corusUrl.toString());
    } else {
      _log.warn("VM was not started dynamically; HTTP protocol will be disabled");
    }
  }

  /**
   * @see org.sapia.corus.interop.client.InteropProtocol#confirmShutdown()
   */
  public void confirmShutdown() throws FaultException, IOException {
    if (_corusUrl == null) {
      return;
    }

    super.doSendConfirmShutdown();
  }

  /**
   * @see org.sapia.corus.interop.client.InteropProtocol#poll()
   */
  public List poll() throws FaultException, IOException {
    if (_corusUrl == null) {
      return EMPTY_LST;
    }

    return super.doSendPoll();
  }

  /**
   * @see org.sapia.corus.interop.client.InteropProtocol#restart()
   */
  public void restart() throws FaultException, IOException {
    if (_corusUrl == null) {
      return;
    }

    super.doSendRestart();
  }

  /**
   * @see org.sapia.corus.interop.client.InteropProtocol#sendStatus(Status)
   */
  public List sendStatus(Status stat) throws FaultException, IOException {
    if (_corusUrl == null) {
      return EMPTY_LST;
    }

    return super.doSendStatus(stat, false);
  }

  /**
   * @see org.sapia.corus.interop.client.InteropProtocol#pollAndSendStatus(Status)
   */
  public List pollAndSendStatus(Status stat) throws FaultException, IOException {
    if (_corusUrl == null) {
      return EMPTY_LST;
    }

    return super.doSendStatus(stat, true);
  }
  
  /**
   * @see ClientStatelessSoapStreamHelper#newStreamConnection()
   */
  protected StreamConnection newStreamConnection() throws IOException {
    if (_corusUrl == null) {
      throw new IOException("corus server URL not specified; VM was probably not started dynamically");
    }

    return new HttpURLStreamConnection(_corusUrl);
  }
}
