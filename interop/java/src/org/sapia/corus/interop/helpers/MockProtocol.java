package org.sapia.corus.interop.helpers;

import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.soap.FaultException;

import java.io.IOException;

import java.util.List;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class MockProtocol extends ClientStatelessSoapStreamHelper {
  protected MockStreamListener _listener;

  protected MockProtocol(String corusPid, MockStreamListener listener) {
    super(corusPid);
    _listener = listener;
  }

  protected StreamConnection newStreamConnection() throws IOException {
    return new MockStreamConnection(_listener);
  }

  public void confirmShutdown() throws FaultException, IOException {
    super.doSendConfirmShutdown();
  }

  public List poll() throws FaultException, IOException {
    return super.doSendPoll();
  }

  public void restart() throws FaultException, IOException {
    super.doSendRestart();
  }

  public List sendStatus(Status stat) throws FaultException, IOException {
    return super.doSendStatus(stat, false);
  }

  public List pollAndSendStatus(Status stat) throws FaultException, IOException {
    return super.doSendStatus(stat, true);
  }
}
