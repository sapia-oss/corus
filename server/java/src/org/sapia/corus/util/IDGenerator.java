package org.sapia.corus.util;


/**
 * Makes unique IDs based on time and internal counter.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class IDGenerator {
  private static int _count = 0;

  public static synchronized String makeId() {
    String id = "" + System.currentTimeMillis() + _count++;

    if (_count >= 999) {
      _count = 0;
    }

    return id;
  }
}
