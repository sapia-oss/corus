package org.sapia.corus.util;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProgressMsg implements java.io.Serializable {
  public static final int DEBUG   = 0;
  public static final int VERBOSE = 1;
  public static final int INFO    = 2;
  public static final int WARNING = 3;
  public static final int ERROR   = 4;
  
  public static final String[] STATUS_LABELS =
    new String[]{
    	"DEBUG", "VERBOSE", "INFO", "WARNING", "ERROR"
    };
  
  private int             _status = INFO;
  private Object          _msg;

  public ProgressMsg(Object msg) {
    _msg = msg;
  }

  public ProgressMsg(Object msg, int status) {
    _msg    = msg;
    _status = status;
  }

  public Object getMessage() {
    return _msg;
  }

  public Throwable getError() {
    return (Throwable) _msg;
  }

  public int getStatus() {
    return _status;
  }

  public boolean isThrowable() {
    return _msg instanceof Throwable;
  }

  public boolean isError() {
    return _status == ERROR;
  }
  
  public static final String getLabelFor(int status){
  	return STATUS_LABELS[status];
  }

  public String toString() {
    return "[ msg=" + _msg + ", level=" + _status + "]";
  }
}
