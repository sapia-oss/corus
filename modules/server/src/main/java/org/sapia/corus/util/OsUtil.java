package org.sapia.corus.util;

/**
 * Provides OS-related utility methods.
 * 
 * @author yduchesne
 * 
 */
public class OsUtil {

  private static final boolean UNIX = System.getProperty("os.name").toLowerCase().contains("nix");
  private static final boolean LINUX = System.getProperty("os.name").toLowerCase().contains("nux");
  private static final boolean AIX = System.getProperty("os.name").toLowerCase().contains("aix");
  private static final boolean BSD = System.getProperty("os.name").toLowerCase().contains("bsd");
  private static final boolean SOLARIS = System.getProperty("os.name").toLowerCase().contains("solaris");
  private static final boolean SUNOS = System.getProperty("os.name").toLowerCase().contains("sunos");
  private static final boolean MAC = System.getProperty("os.name").toLowerCase().contains("mac");
  private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

  private OsUtil() {
  }

  /**
   * @return <code>true</code> if the OS is in the Unix family.
   */
  public static boolean isUnixFamily() {
    return UNIX || LINUX || AIX || BSD || SOLARIS || SUNOS || MAC;
  }

  /**
   * @return <code>true</code> if the OS is in the Windows family.
   */
  public static boolean isWindowsFamily() {
    return WINDOWS;
  }

}
