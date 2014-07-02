package org.sapia.corus.sigar;

import org.hyperic.sigar.Sigar;
import org.sapia.corus.core.ModuleHelper;

/**
 * Implements the {@link SigarModule} interface: initializes {@link Sigar} only if
 * the <code>java.library.path</code> property has been set, and if it holds the path
 * to the Sigar native libraries.
 *
 * @author yduchesne
 *
 */
public class SigarModuleImpl extends ModuleHelper implements SigarModule {

  private static final String SIGAR_SUBDIR = "extra-lib/sigar";

  private static final String JAVA_LIB_PATH = System.getProperty("java.library.path");

  private Sigar sigar;

  @Override
  public String getRoleName() {
    return SigarModule.ROLE;
  }

  @Override
  public void init() throws Exception {
    if (JAVA_LIB_PATH != null && JAVA_LIB_PATH.contains(SIGAR_SUBDIR)) {
      sigar = new Sigar();
      SigarSupplier.set(sigar);
    }
  }

  @Override
  public void dispose() throws Exception {
    if (sigar != null) {
      sigar.close();
    }
  }
}
