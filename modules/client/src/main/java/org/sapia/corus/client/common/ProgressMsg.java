package org.sapia.corus.client.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;

import org.sapia.ubik.util.Assertions;

/**
 * This class consists of a progress message, holding a status and an arbitrary object acting as the "message".
 * 
 * @author Yanick Duchesne
 */
public class ProgressMsg implements Externalizable {

  static final long serialVersionUID = 1L;
  
  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;

  public static final int DEBUG    = 0;
  public static final int VERBOSE  = 1;
  public static final int INFO     = 2;
  public static final int WARNING  = 3;
  public static final int ERROR    = 4;

  public static final String[] STATUS_LABELS = new String[] { "DEBUG", "VERBOSE", "INFO", "WARNING", "ERROR" };

  private long   timestamp = System.nanoTime();
  private int    status   = INFO;
  private Object msg;
 
  /** Do not call. Meant for Externalizable. */
  public ProgressMsg() {
  }

  public ProgressMsg(Object msg) {
    if (msg instanceof InvocationTargetException) {
      this.msg = ((InvocationTargetException) msg).getCause();
    } else {
      this.msg = msg;
    }
  }

  public ProgressMsg(Object msg, int status) {
    this(msg);
    this.status = status;
  }
  
  /**
   * @return the timestamp corresponding to the time at which this instance was created 
   * - initially obtained with {@link System#nanoTime()}.
   */
  public long getTimestamp() {
    return timestamp;
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
  public Throwable getThrowable() {
    return (Throwable) msg;
  }

  /**
   * @return this instance's status.
   */
  public int getStatus() {
    return status;
  }

  /**
   * @return <code>true</code> if this instance's message object is an instance
   *         of {@link Throwable}
   * 
   * @see #getThrowable()
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

  public static final String getLabelFor(int status) {
    Assertions.isTrue(status >= 0 && status < STATUS_LABELS.length, "Invalid value, expected to be between %s and %s", 0, STATUS_LABELS.length - 1);
    return STATUS_LABELS[status];
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    
    if (inputVersion == VERSION_1) {
      status = in.readInt();
      msg    = in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeInt(status);
    out.writeObject(msg);
  }

  // --------------------------------------------------------------------------
  // Object overridde
  
  public String toString() {
    return "[ msg=" + msg + ", level=" + status + "]";
  }
}
