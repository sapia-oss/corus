package org.sapia.corus.client.cli;

import java.io.File;

/**
 * Abstracts the file system.
 * 
 * @author yduchesne
 *
 */
public interface ClientFileSystem {

  /**
   * @return the base directory.
   */
  public File getBaseDir();
  
  /**
   * Returns the file object corresponding to the given file name. The actual physical
   * resource may or may not exist, it is up to the calling code to perform any
   * required validation.
   * 
   * @param name a file name.
   * @return the {@link File} object corresponding to the given name.
   */
  public File getFile(String name);
}
