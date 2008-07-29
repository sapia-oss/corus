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
public class Envelope {
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////  INSTANCE ATTRIBUTES  /////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /** The header of this envelope. */
  private Header _theHeader;

  /** The body of this envelope. */
  private Body _theBody;

  /////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////  CONSTRUCTORS  /////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new Envelope instance.
   */
  public Envelope() {
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  ACCESSOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the header of this envelope.
   *
   * @return The header of this envelope.
   */
  public Header getHeader() {
    return _theHeader;
  }

  /**
   * Returns the body of this envelope.
   *
   * @return The  body of this envelope.
   */
  public Body getBody() {
    return _theBody;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////  MUTATOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Changes the header of this envelope.
   *
   * @param aHeader The new header.
   */
  public void setHeader(Header aHeader) {
    _theHeader = aHeader;
  }

  /**
   * Changes the body of this envelope.
   *
   * @param aBody The new body.
   */
  public void setBody(Body aBody) {
    _theBody = aBody;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  OVERRIDEN METHODS  //////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a string representation of this envelope.
   *
   * @return A string representation of this envelope.
   */
  public String toString() {
    StringBuffer aBuffer = new StringBuffer(super.toString());
    aBuffer.append("[header=").append(_theHeader).append(" body=")
           .append(_theBody).append("]");

    return aBuffer.toString();
  }
}
