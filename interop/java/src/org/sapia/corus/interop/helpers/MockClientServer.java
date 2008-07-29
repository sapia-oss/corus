package org.sapia.corus.interop.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


//import java.util.ArrayList;
//import java.util.List;
//
//import org.sapia.corus.interop.ConfirmShutdown;
//import org.sapia.corus.interop.Poll;
//import org.sapia.corus.interop.Process;
//import org.sapia.corus.interop.Restart;
//import org.sapia.corus.interop.Status;

/**
 * Use an instance of this class to simulate client-server SOAP interaction.
 * This class implements <code>InteropProtocol</code> insterface and allows
 * to pass in a <code>RequestListener</code> that simulates the server.
 * <p>
 * To perform tests, implement your own <code>RequestListener</code>, and use
 * the client methods (from the <code>InteropProtocol</code> interface) to send
 * requests to the "server".
 * <p>
 * <pre>
 * InteropProtocol interop = new MockClientServer(new MyRequestListener());
 * interop.poll();
 * interop.confirmShutdown();
 * interop.restart();
 * Status stat = new Status();
 * interop.sendStatus(stat);
 * </pre>
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MockClientServer extends MockProtocol implements MockStreamListener {
  ServerStatelessSoapStreamHelper _server;

  public MockClientServer(RequestListener listener) {
    super("testCorusPid", null);
    super._listener = this;
    _server         = new ServerStatelessSoapStreamHelper(listener, "mockServer");
  }

  public void onRequest(InputStream req, OutputStream res)
                 throws IOException {
    try {
      _server.processRequest(req, res);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

  protected StreamConnection newStreamConnection() throws IOException {
    return new MockStreamConnection(this);
  }
}
