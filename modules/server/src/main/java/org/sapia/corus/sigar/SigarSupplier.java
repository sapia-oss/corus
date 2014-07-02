package org.sapia.corus.sigar;

import org.hyperic.sigar.Sigar;

/**
 * Supplies the {@link Sigar} instance.
 *
 * @author yduchesne
 *
 */
public class SigarSupplier {

  private static Sigar sigar;

  /**
   * @return the {@link Sigar} instance.
   */
  public static Sigar get() {
    if (sigar == null) {
      throw new IllegalStateException("Sigar instance not initialized");
    }
    return sigar;
  }

  /**
   * @return <code>true</code> if the {@link Sigar} instance was initialized and
   * assigned to this instance.
   */
  public static boolean isSet() {
    return sigar != null;
  }

  /**
   * @param sigar the {@link Sigar} instance.
   */
  static void set(Sigar instance) {
    sigar = instance;
  }

}
