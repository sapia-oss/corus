package org.sapia.corus.interop.client;


/**
 * A generator of identifiers for commands and requests. The generator will
 * create numeric identifiers up to a value of 999999; then the internal
 * counter will be reset.
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CyclicIdGenerator {

  public static final int MAX_ID = 999999;

  private static int _commandCount = 0;
  private static int _requestCount = 0;

  /**
   * Generates the next command identifier.
   * 
   * @return The next command identifier.
   */
  public static synchronized String newCommandId() {
    if (_commandCount >= MAX_ID) {
      _commandCount = 0;
    }

    return String.valueOf(++_commandCount);
  }

  /**
   * Generates the next request identifier.
   * 
   * @return The next request identifier.
   */
  public static synchronized String newRequestId() {
    if (_requestCount >= MAX_ID) {
      _requestCount = 0;
    }

    return String.valueOf(++_requestCount);
  }
}
