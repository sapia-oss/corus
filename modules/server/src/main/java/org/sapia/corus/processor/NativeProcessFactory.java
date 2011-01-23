package org.sapia.corus.processor;


/**
 * A factory of <code>NativeProcess</code> instance.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class NativeProcessFactory {
  
  /**
   * @return a <code>NativeProcess</code> instance.
   */
  public static NativeProcess newNativeProcess() {
    if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
      return new WindowsProcess();
    } else {
      return new UnixProcess();
    }
  }
}
