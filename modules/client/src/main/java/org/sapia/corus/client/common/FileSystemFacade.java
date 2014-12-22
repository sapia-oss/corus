package org.sapia.corus.client.common;

import java.io.File;

/**
 * Abstracts the file system.
 * 
 * @author yduchesne
 *
 */
public interface FileSystemFacade {

  /**
   * @param path a path for which to obtain a {@link File} instance.
   * @return a new {@link File} instance.
   */
  public File getFile(String path);
  
  // ==========================================================================
  
  /**
   * Implements the {@link FileSystemFacade} interface over the "real" file system.
   * 
   * @author yduchesne
   *
   */
  public static class DefaultFileSystemFacade implements FileSystemFacade {
    
    @Override
    public File getFile(String path) {
      return new File(path);
    }
    
    /**
     * @return a new instance of this class.
     */
    public static FileSystemFacade newInstance() {
      return new DefaultFileSystemFacade();
    } 
  }
}
