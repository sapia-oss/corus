package org.sapia.corus.interop.client;

import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.api.StatusRequestListener;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestStatusListener implements StatusRequestListener {
  boolean called;

  public void onStatus(Status status) {
    Context t = new Context();
    t.setName("TestContext");
    t.addParam(new Param("testName", "testValue"));
    status.addContext(t);
    called = true;
  }
}
