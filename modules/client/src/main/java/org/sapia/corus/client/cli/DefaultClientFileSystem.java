package org.sapia.corus.client.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.sapia.corus.client.common.CliUtils;

/**
 * Default {@link ClientFileSystem} implementation.
 * 
 * @author yduchesne
 * 
 */
public class DefaultClientFileSystem implements ClientFileSystem {

  private File baseDir;

  /**
   * Constructs a {@link DefaultClientFileSystem} with the current process
   * directory as a base directory.
   */
  public DefaultClientFileSystem() {
    this(new File(System.getProperty("user.dir")));
  }

  /**
   * @param baseDir
   *          a {@link File} corresponding to a base directory.
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
    if (CliUtils.isAbsolute(name)) {
      return new File(name);
    } else {
      return new File(baseDir, name);
    }
  }

  @Override
  public void changeBaseDir(String dirName) throws IOException, FileNotFoundException {
    File dir;

    if (CliUtils.isAbsolute(dirName)) {
      dir = new File(dirName);
    } else if (dirName.equals(".")) {
      return;
    } else if (dirName.equals("..")) {
      dir = baseDir.getParentFile();
      if (dir == null) {
        throw new IOException(String.format("Current directory (%s) has no parent", baseDir.getAbsolutePath()));
      }
    } else {
      dir = new File(baseDir, dirName);
    }
    if (!dir.isDirectory()) {
      throw new IOException("Not a directory: " + dir.getAbsolutePath());
    }
    if (!dir.exists()) {
      throw new FileNotFoundException("Directory does not exist" + dir.getAbsolutePath());
    }
    if (!dir.canRead()) {
      throw new IOException("You do not have access to this directory");
    }
    baseDir = dir;
  }
}
