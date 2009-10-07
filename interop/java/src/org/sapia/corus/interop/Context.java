package org.sapia.corus.interop;


// Import of Sun's JDK classes
// ---------------------------
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Context implements Serializable, Comparator<Param> {
  
  static final long serialVersionUID = 1L;
  
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////  INSTANCE ATTRIBUTES  /////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /** The name of this context. */
  private String _theName;

  /** The list of params of this context. */
  private List<Param> _theParams;

  /////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////  CONSTRUCTORS  /////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new Context instance.
   */
  public Context() {
    _theParams = new ArrayList<Param>();
  }

  /**
   * Creates a new Context instance.
   */
  public Context(String aName) {
    _theName  = aName;
    _theParams = new ArrayList<Param>();
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  ACCESSOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return _theName;
  }

  public List<Param> getParams() {
    Collections.sort(_theParams, this);
    return _theParams;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////  MUTATOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  public void setName(String aName) {
    _theName = aName;
  }

  public void addParam(Param anParam) {
    _theParams.add(anParam);
  }

  public void removeParam(Object anParam) {
    _theParams.remove(anParam);
  }

  public void clearParams() {
    _theParams.clear();
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  OVERRIDEN METHODS  //////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  public String toString() {
    StringBuffer aBuffer = new StringBuffer(super.toString());
    aBuffer.append("[name=").append(_theName).append(" params=").append(_theParams)
           .append("]");

    return aBuffer.toString();
  }

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Param param1, Param param2) {
    return param1.getName().compareTo(param2.getName());
  }
}
