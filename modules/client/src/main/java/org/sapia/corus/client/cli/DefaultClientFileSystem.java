package org.sapia.corus.client.cli;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Default {@link ClientFileSystem} implementation.
 * 
 * @author yduchesne
 *
 */
public class DefaultClientFileSystem implements ClientFileSystem {
 
  private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[a-z]:");
  
  private File baseDir;
  
  /**
   * Constructs a {@link DefaultClientFileSystem} with the current process directory
   * as a base directory.
   */
  public DefaultClientFileSystem() {
    this(new File("."));
  }
  
  /**
   * @param baseDir a {@link File} corresponding to a base directory.
   */
  public DefaultClientFileSystem(File baseDir) {
    this.baseDir = baseDir;
  }
  
  @Override
  public File getBaseDir() {
    return baseDir;
  }
  
  @Override
  public File getFile(String name) {
    if (isAbsolute(name)) {
      return new File(name);
    } else {
      return new File(baseDir, name);
    }
  }

  /**
   * @param fileName a file name.
   * @return <code>true</code> if the given name corresponding to an absolute file.
   */
  static boolean isAbsolute(String fileName) {
    String theFileName = fileName.trim();
    return 
        theFileName.length() > 0
        && 
        (WINDOWS_DRIVE_PATTERN.matcher(theFileName).find()
         || theFileName.startsWith("/"));
  }
}
