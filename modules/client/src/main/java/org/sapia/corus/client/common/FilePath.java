package org.sapia.corus.client.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.util.Assertions;

/**
 * A utility class used to build file paths dynamically.
 * 
 * @author yduchesne
 * 
 */
public class FilePath {

  private List<String> dirs = new ArrayList<String>();
  private String file;

  /**
   * @param dir
   *          a directory path, relative to this instance.
   * @return this instance.
   */
  public FilePath addDir(String dir) {
    dirs.add(FileUtils.fixFileSeparators(dir));
    return this;
  }
  
  /**
   * Adds the ${user.home} directory to this instance.
   * 
   * @return this instance.
   */
  public FilePath addUserHome() {
    dirs.add(FileUtils.fixFileSeparators(System.getProperty("user.home")));
    return this;
  }
  
  /**
   * Adds the ${user.home}/.corus directory to this instance.
   * 
   * @return this instance.
   */
  public FilePath addCorusUserDir() {
    dirs.add(FileUtils.fixFileSeparators(System.getProperty("user.home")));
    dirs.add(".corus");
    return this;
  }
  
  /**
   * Adds the directory corresponding to the <code>java.io.tmpdir</code> system property.
   * 
   * @return this instance.
   */
  public FilePath addJvmTempDir() {
    String tmp = System.getProperty("java.io.tmpdir");
    Assertions.illegalState(tmp == null, "java.io.tmpdir system property not set");
    dirs.add(tmp);
    return this;
  }

  /**
   * @param file
   *          a file name.
   * @return this instance.
   */
  public FilePath setRelativeFile(String file) {
    this.file = FileUtils.fixFileSeparators(file);
    return this;
  }

  /**
   * Creates a {@link File} instance, relative to the given directory.
   * 
   * @return the {@link File} corresponding to this instance.
   */
  public File createFileFrom(File baseDir) {
    return new File(createFilePathFrom(baseDir));
  }

  /**
   * Creates a file path instance, relative to the given directory.
   * 
   * @return the path corresponding to this instance.
   */
  public String createFilePathFrom(File baseDir) {
    File fixedDir = new File(FileUtils.fixFileSeparators(baseDir.getAbsolutePath()));
    Assertions.isTrue(fixedDir.isDirectory(), "File does not correspond to a directory: " + fixedDir.getAbsolutePath());
    StringBuilder sb = new StringBuilder();
    sb.append(fixedDir.getAbsolutePath()).append(File.separator);
    if (file != null) {
      sb.append(file);
    }
    return sb.toString();
  }

  /**
   * @return the {@link File} corresponding to this instance.
   */
  public File createFile() {
    return new File(createFilePath());
  }

  /**
   * @return the path corresponding to this instance.
   */
  public String createFilePath() {
    StringBuilder sb = new StringBuilder();
    for (String dir : dirs) {
      sb.append(dir);
      if (!dir.endsWith(File.separator)) {
        sb.append(File.separator);
      }
    }
    if (file != null) {
      sb.append(file);
    }
    return sb.toString();
  }
  
  /**
   * @return a new instance of this class.
   */
  public static FilePath newInstance() {
    return new FilePath();
  }
}
