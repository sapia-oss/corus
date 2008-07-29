package org.sapia.corus.db;

import org.sapia.corus.util.NestedRuntimeException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class IORuntimeException extends NestedRuntimeException {
  /**
   * Constructor for IORuntimeException.
   * @param msg
   */
  public IORuntimeException(String msg) {
    super(msg);
  }

  /**
   * Constructor for IORuntimeException.
   * @param err
   */
  public IORuntimeException(Throwable err) {
    super(err);
  }

  /**
   * Constructor for IORuntimeException.
   * @param msg
   * @param err
   */
  public IORuntimeException(String msg, Throwable err) {
    super(msg, err);
  }
}
