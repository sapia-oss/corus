package org.sapia.corus.interop.client;


/**
 * Implements the <code>Log</code> interface over stdout. The default
 * log level is INFO.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StdoutLog implements Log {
  public static final int DEBUG   = 0;
  public static final int INFO    = 1;
  public static final int WARNING = 2;
  public static final int FATAL   = 3;
  private int             _lvl = DEBUG;

  public void debug(Object o) {
    if (isValid(DEBUG)) {
      System.out.println("INTEROP [DEBUG] " + o);
    }
  }

  public void info(Object o) {
    if (isValid(INFO)) {
      System.out.println("INTEROP [INFO] " + o);
    }
  }

  public void info(Object o, Throwable t) {
    if (isValid(INFO)) {
      System.out.println("INTEROP [INFO] " + o);
      t.printStackTrace(System.out);
    }
  }  

  public void warn(Object o, Throwable t) {
    if (isValid(WARNING)) {
      System.out.println("INTEROP [WARNING] " + o);
      t.printStackTrace(System.out);
    }
  }

  public void warn(Object o) {
    if (isValid(WARNING)) {
      System.out.println("INTEROP [WARNING] " + o);
    }
  }

  public void fatal(Object o, Throwable t) {
    if (isValid(FATAL)) {
      System.out.println("INTEROP [FATAL] " + o);
      t.printStackTrace(System.out);
    }
  }

  public void fatal(Object o) {
    if (isValid(FATAL)) {
      System.out.println("INTEROP [FATAL] " + o);
    }
  }

  public boolean isValid(int lvl) {
    return lvl >= _lvl;
  }

  /**
   * @param level a debug level
   *
   * @see #DEBUG
   * @see #INFO
   * @see #WARNING
   * @see #FATAL
   */
  public void setLevel(int level) {
    _lvl = level;
  }
}
