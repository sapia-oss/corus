package org.sapia.corus.interop.helpers;

import junit.framework.TestCase;

import org.sapia.corus.interop.Status;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HelpersTest extends TestCase {
  MockClientServer    _mock;
  TestRequestListener _listener;

  public HelpersTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    _listener = new TestRequestListener();
    _mock     = new MockClientServer(_listener);
  }

  public void testPoll() throws Exception {
    _mock.poll();
    super.assertTrue(_listener.poll);
  }

  public void testStatus() throws Exception {
    Status stat = new Status();
    _mock.sendStatus(stat);
    super.assertTrue(_listener.status);
  }

  public void testConfirmShutdown() throws Exception {
    _mock.confirmShutdown();
    super.assertTrue(_listener.confirm);
  }

  public void testRestart() throws Exception {
    _mock.restart();
    super.assertTrue(_listener.restart);
  }
}
