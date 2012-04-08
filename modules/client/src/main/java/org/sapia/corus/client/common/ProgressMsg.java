package org.sapia.corus.client.common;

/**
 * This class consist of a progress message.
 * 
 * @author Yanick Duchesne
 */
public class ProgressMsg implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  public static final int DEBUG   = 0;
  public static final int VERBOSE = 1;
  public static final int INFO    = 2;
  public static final int WARNING = 3;
  public static final int ERROR   = 4;
  
  public static final String[] STATUS_LABELS =
    new String[]{
    	"DEBUG", "VERBOSE", "INFO", "WARNING", "ERROR"
    };
  
  private int             status = INFO;
  private Object          msg;
  
  public ProgressMsg(Object msg) {
    this.msg = msg;
  }

  public ProgressMsg(Object msg, int status) {
    this(msg);
    this.status = status;
  }

  /**
   * @return the actual message object (may be a {@link Throwable} instance).
   */
  public Object getMessage() {
    return msg;
  }

  /**
   * @return this instance's message, cast as a {@link Throwable}.
   * @see #isThrowable()
   */
  public Throwable getError() {
    return (Throwable) msg;
  }

  /**
   * @return this instance's status.
   */
  public int getStatus() {
    return status;
  }

  /**
   * @return <code>true</code> if this instance's message object
   * is an instance of {@link Throwable}
   * 
   * @see #getError()
   */
  public boolean isThrowable() {
    return msg instanceof Throwable;
  }

  /**
   * 
   * @return <code>true</code> if this instance's status indicates an error.
   */
  public boolean isError() {
    return status == ERROR;
  }
  
  public static final String getLabelFor(int status){
  	return STATUS_LABELS[status];
  }

  public String toString() {
    return "[ msg=" + msg + ", level=" + status + "]";
  }
}
