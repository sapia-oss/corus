package org.sapia.corus.sigar;

import org.hyperic.sigar.SigarProxy;

/**
 * Supplies the {@link SigarProxy} instance.
 *
 * @author yduchesne
 *
 */
public class SigarSupplier {

  private static SigarProxy sigar;

  /**
   * @return the {@link SigarProxy} instance.
   */
  public static SigarProxy get() {
    if (sigar == null) {
      throw new IllegalStateException("Sigar instance not initialized");
    }
    return sigar;
  }

  /**
   * @return <code>true</code> if the {@link SigarProxy} instance was initialized and
   * assigned to this instance.
   */
  public static boolean isSet() {
    return sigar != null;
  }

  /**
   * @param sigar the {@link SigarProxy} instance.
   */
  static void set(SigarProxy instance) {
    sigar = instance;
  }

}
