package org.sapia.corus.sigar;

import java.io.File;
import java.util.StringTokenizer;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarProxy;
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
      StringTokenizer tk = new StringTokenizer(JAVA_LIB_PATH, ":;");
      while (tk.hasMoreElements()) {
        String t = tk.nextToken();
        if (t.contains(SIGAR_SUBDIR)) {
          File f = new File(t);
          if (f.exists()) {
            File[] content = f.listFiles();
            if (content != null && content.length > 0) {
              logger().info("Initializing SIGAR");
              SigarSupplier.set(newSigarInstance());
              break;
            } else {
              logger().info("Director for SIGAR native libs is empty: " + t);
            }
          } else {
            logger().info("Directory containing SIGAR native libs does not exist: " + t);
          }
        }
      }
    } else {
      logger().info("java.library.path not set or does not contain path to SIGAR native libs");
    }
  }

  @Override
  public void dispose() throws Exception {
    if (sigar != null) {
      sigar.close();
    }
  }

  protected SigarProxy newSigarInstance() {
    return new Sigar();
  }
}
