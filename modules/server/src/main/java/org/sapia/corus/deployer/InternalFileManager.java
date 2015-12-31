package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;

import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;

/**
 * Extends the {@link FileManager} interface.
 * 
 * @author yduchesne
 * 
 */
public interface InternalFileManager extends FileManager {

  /**
   * @param info
   *          a {@link FileInfo} the {@link FileInfo} corresponding to the file
   *          to deploy.
   * @return the {@link File} corresponding to the given {@link FileInfo}.
   * @throws FileNotFoundException
   *           if no such file could be found.
   */
  public File getFile(FileInfo info) throws FileNotFoundException;

}
