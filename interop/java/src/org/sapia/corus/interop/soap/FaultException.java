package org.sapia.corus.interop.soap;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class FaultException extends Exception {
  private Fault _fault;

  public FaultException(Fault f) {
    _fault = f;
  }

  public String getMessage() {
    return _fault.getFaultstring() + System.getProperty("line.separator") +
           _fault.getDetail();
  }

  public Fault getFault() {
    return _fault;
  }
}
