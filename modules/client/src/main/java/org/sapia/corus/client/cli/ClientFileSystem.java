package org.sapia.corus.client.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
   * @param dirName a directory name.
   * @throws IOException if the given directory name does not correspond to a directory.
   * @throws FileNotFoundException if no such directory exists. 
   */
  public void changeBaseDir(String dirName) throws IOException, FileNotFoundException;
  
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
