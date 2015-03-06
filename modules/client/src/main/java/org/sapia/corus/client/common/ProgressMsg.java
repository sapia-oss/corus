package org.sapia.corus.client.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;

/**
 * This class consists of a progress message, holding a status and an arbitrary object acting as the "message".
 * 
 * @author Yanick Duchesne
 */
public class ProgressMsg implements Externalizable {

  static final long serialVersionUID = 1L;

  public static final int DEBUG = 0;
  public static final int VERBOSE = 1;
  public static final int INFO = 2;
  public static final int WARNING = 3;
  public static final int ERROR = 4;

  public static final String[] STATUS_LABELS = new String[] { "DEBUG", "VERBOSE", "INFO", "WARNING", "ERROR" };

  private int status = INFO;
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
    return STATUS_LABELS[status];
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    status = in.readInt();
    msg    = in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(status);
    out.writeObject(msg);
  }

  // --------------------------------------------------------------------------
  // Object overridde
  
  public String toString() {
    return "[ msg=" + msg + ", level=" + status + "]";
  }
}
