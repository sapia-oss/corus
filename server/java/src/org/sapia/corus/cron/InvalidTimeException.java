package org.sapia.corus.cron;

import org.sapia.corus.exceptions.LogicException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InvalidTimeException extends LogicException {
  /**
   * Constructor for InvalidTimeException.
   * @param msg
   */
  public InvalidTimeException(String msg) {
    super(msg);
  }

  /**
   * Constructor for InvalidTimeException.
   * @param msg
   * @param err
   */
  public InvalidTimeException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for InvalidTimeException.
   * @param err
   */
  public InvalidTimeException(Throwable err) {
    super(err);
  }
}
