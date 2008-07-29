package org.sapia.corus.interop.soap;


/**
 *
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Fault {
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////  INSTANCE ATTRIBUTES  /////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  private String _theCode;
  private String _theActor;
  private String _theString;
  private Object _theDetail;

  /////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////  CONSTRUCTORS  /////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new Fault instance.
   */
  public Fault() {
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  ACCESSOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  public String getFaultcode() {
    return _theCode;
  }

  public String getFaultactor() {
    return _theActor;
  }

  public String getFaultstring() {
    return _theString;
  }

  public Object getDetail() {
    return _theDetail;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////  MUTATOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  public void setFaultcode(String aCode) {
    _theCode = aCode;
  }

  public void setFaultactor(String anActor) {
    _theActor = anActor;
  }

  public void setFaultstring(String aString) {
    _theString = aString;
  }

  public void setDetail(Object aDetail) {
    _theDetail = aDetail;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  OVERRIDEN METHODS  //////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a string representation of this fault.
   *
   * @return A string representation of this fault.
   */
  public String toString() {
    StringBuffer aBuffer = new StringBuffer(super.toString());
    aBuffer.append("[faultcode=").append(_theCode).append(" faultactor=")
           .append(_theActor).append(" faultstring=").append(_theString)
           .append(" detail=").append(_theDetail).append("]");

    return aBuffer.toString();
  }
}
