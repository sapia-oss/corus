package org.sapia.corus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * A utility class used to build file paths dynamically.
 * @author yduchesne
 *
 */
public class FilePath {

  private List<String> dirs = new ArrayList<String>();
  private String       file;
  
  /**
   * @param dir a directory path, relative to this instance.
   * @return this instance.
   */
  public FilePath addDir(String dir) {
    dirs.add(dir);
    return this;
  }
  
  /**
   * @param file a file name.
   * @return this instance.
   */
  public FilePath setRelativeFile(String file) {
    this.file = file;
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
    Assert.isTrue(
        baseDir.isDirectory(), 
        "File does not correspond to a directory: " + baseDir.getAbsolutePath());
    StringBuilder sb = new StringBuilder();
    sb.append(baseDir.getAbsolutePath()).append(File.separator);
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
