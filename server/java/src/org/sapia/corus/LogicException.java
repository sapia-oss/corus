package org.sapia.corus;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class LogicException extends CorusException {
  /**
   * Constructor for LogicException.
   * @param msg
   */
  public LogicException(String msg) {
    super(msg);
  }

  /**
   * Constructor for LogicException.
   * @param msg
   * @param err
   */
  public LogicException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for LogicException.
   * @param err
   */
  public LogicException(Throwable err) {
    super(err);
  }
}
