package org.sapia.corus.os;

import org.sapia.corus.util.OsUtil;

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
    if (OsUtil.isWindowsFamily()) {
      return new WindowsProcess();
    } else {
      return new UnixProcess();
    }
  }
}
