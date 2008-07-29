package org.sapia.corus.interop.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface MockStreamListener {
  public void onRequest(InputStream req, OutputStream res)
                 throws IOException;
}
