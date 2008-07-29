package org.sapia.corus;

import org.sapia.corus.util.NestedException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusException extends NestedException {
  public CorusException(String msg) {
    super(msg);
  }

  public CorusException(String msg, Throwable err) {
    super(msg, err);
  }

  public CorusException(Throwable err) {
    super(err);
  }
}
