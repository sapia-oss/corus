package org.sapia.corus.interop.client;

import junit.framework.TestCase;

import org.sapia.corus.interop.api.Consts;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InteropClientTest extends TestCase {
  static {
    System.setProperty(Consts.CORUS_PID, "123");
    System.setProperty(Consts.CORUS_POLL_INTERVAL, "1");
    System.setProperty(Consts.CORUS_STATUS_INTERVAL, "2");
    System.setProperty(Consts.CORUS_CLIENT_ANALYSIS_INTERVAL, "1");
  }

  public InteropClientTest(String name) {
    super(name);
  }

  public void testPollAndStatus() throws Exception {
    TestStatusListener listener = new TestStatusListener();
    InteropClient      cli   = InteropClient.getInstance();
    TestProtocol       proto = new TestProtocol();
    cli._proto = null;
    cli.setProtocol(proto);
    cli.addStatusRequestListener(listener);
    Thread.sleep(6000);
    super.assertTrue("Poll count is: " + proto.pollCount, proto.pollCount >= 4);
    super.assertTrue("Status count is: " + proto.statCount, proto.statCount >= 2);
    super.assertTrue("Status request listener was not called", listener.called);
  }

  public void testShutdown() throws Exception {
    TestShutdownListener listener = new TestShutdownListener();
    InteropClient        cli   = InteropClient.getInstance();
    TestProtocol         proto = new TestProtocol();
    cli._proto = null;
    cli.setProtocol(proto);
    cli._exitSystemOnShutdown = false;
    cli.addShutdownListener(listener);
    cli.shutdown();
    super.assertTrue("Shutdown listener was not called", listener.called);
  }
}
