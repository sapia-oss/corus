package org.sapia.corus.client.services.processor;

import java.io.Serializable;

import org.sapia.ubik.util.Strings;

/**
 * Holds flags indicating how a process (or set of processes) should be killed.
 * 
 * @author yduchesne
 *
 */
public class KillPreferences implements Serializable {
  
  static final long serialVersionUID = 1L;
  
  boolean suspend, hard;
 
  /**
   * Sets this instance's <code>suspend</code> flag to <code>true</code>.
   * 
   * @return this instance.
   */
  public KillPreferences suspend() {
    this.suspend = true;
    return this;
  }
  
  /**
   * @return <code>true</code> if the process should be suspended.
   */
  public boolean isSuspend() {
    return suspend;
  }
  
  /**
   * Sets this instance's <code>hard</code> kill flag to <code>true</code>.
   * 
   * @return this instance.
   */
  public KillPreferences hard() {
    this.hard = true;
    return this;
  }
  
  /**
   * @return <code>true</code> if the process should be kill through an OS kill signal.
   */
  public boolean isHard() {
    return hard;
  }
  
  /**
   * @param hard <code>true</code> if the process should be killed through an OS kill signal,
   * <code>false</code> otherwise.
   * @return this instance.
   */
  public KillPreferences setHard(boolean hard) {
    this.hard = hard;
    return this;
  }
  
  /**
   * @param suspend <code>true</code> if the process should be suspended, <code>false</code>
   * otherwise.
   * @return this instance.
   */
  public KillPreferences setSuspend(boolean suspend) {
    this.suspend = suspend;
    return this;
  }
  
  /**
   * @return a new instance of this class.
   */
  public static KillPreferences newInstance() {
    return new KillPreferences();
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "hard", hard, "suspend", suspend);
  }
}
