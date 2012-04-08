package org.sapia.corus.os;

/**
 * A factory of {@link NativeProcess} instance. 
 *
 * @author Yanick Duchesne
 */
public class NativeProcessFactory {
  
  /**
   * @return a NativeProcess instance corresponding to the OS.
   */
  public static NativeProcess newNativeProcess() {
    if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
      return new WindowsProcess();
    } else {
      return new UnixProcess();
    }
  }
}
