package org.sapia.corus.interop.client;


/**
 * Defines logging behavior.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Log {
  public void debug(Object o);

  public void info(Object o);

  public void info(Object o, Throwable t);
  
  public void warn(Object o);

  public void warn(Object o, Throwable t);

  public void fatal(Object o);

  public void fatal(Object o, Throwable t);
}
