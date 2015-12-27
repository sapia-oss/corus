package org.sapia.corus.client.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

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
    String fixedDir = FileUtil.fixFileSeparators(dir);
    if (!fixedDir.isEmpty() && fixedDir.charAt(0) == File.separatorChar) {
      dirs.add("");
      if (fixedDir.length() > 1) {
        dirs.add(fixedDir.substring(1));
      }
    } else if (FileUtil.isWindowsDrive(fixedDir)) {
      dirs.add(fixedDir.substring(0, 2));
      if (fixedDir.length() > 4) {
        dirs.add(fixedDir.substring(3));
      }
    } else {
      dirs.add(fixedDir);
    }
    
    return this;
  }
  
  /**
   * @return a new {@link FilePath}, holding only the directories.
   */
  public FilePath getDirectoriesAsPath() {
    FilePath dirCopy = new FilePath();
    for (String d : dirs) {
      dirCopy.addDir(d);
    }
    return dirCopy;
  }
  
  /**
   * @return an unmodifiable {@link List} holding this instance's directories.
   */
  public List<String> getDirectories() {
    return Collections.unmodifiableList(dirs);
  }

  /**
   * @param file
   *          a file name.
   * @return this instance.
   */
  public FilePath setRelativeFile(String file) {
    this.file = FileUtil.fixFileSeparators(file);
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
    File fixedDir = new File(FileUtil.fixFileSeparators(baseDir.getAbsolutePath()));
    Assertions.isTrue(fixedDir.isDirectory(), "File does not correspond to a directory: " + fixedDir.getAbsolutePath());
    StringBuilder sb = new StringBuilder();
    sb.append(fixedDir.getAbsolutePath());
    if (file != null) {
      if (sb.length() > 0) {
        sb.append(File.separator);
      }
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
    for (int i = 0; i < dirs.size(); i++) {
      if (i > 0) {
        String d = dirs.get(i);
        if (d.isEmpty() || d.charAt(d.length() - 1) != File.separatorChar) {
          sb.append(File.separator);
        }
      }
      sb.append(dirs.get(i));
    }
    if (file != null) {
      if (!dirs.isEmpty()) {
        sb.append(File.separator);
      }
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
  
  /**
   * @return the number of directories in the path.
   */
  public int getDirCount() {
    return dirs.size();
  }
  
  /**
   * @return <code>true</code> if this instance has no directory.
   */
  public boolean isEmpty() {
    return dirs.size() == 0;
  }
  
  /**
   * @return <code>true</code> if this instance has directories.
   */
  public boolean notEmpty() {
    return dirs.size() > 0;
  }
  
  /**
   * @return <code>true</code> if this instance represents an absolute path.
   */
  public boolean isAbsolute() {
    if (!dirs.isEmpty()) {
      if (dirs.get(0).isEmpty()) {
        return true;
      } else {
        return FileUtil.isWindowsDrive(dirs.get(0));
      }
    }
    return false;
  }
  
  /**
   * Returns the overall number of parts in the path, meaning: the number of nested
   * directories. If a file is specified add the end, 1 is added to the number
   * of directories.
   * <p>
   * If this instance has no file specified, the this method will return the same 
   * result as the {@link #getDirCount()} method.
   * 
   * @return the number of parts in the path.
   */
  public int length() {
    return file == null ? dirs.size() : dirs.size() + 1;
  }
  
  /**
   * @return <code>true</code> if this instance has a relative file 
   * (i.e: relative to the directories that this instance holds).
   * @see #setRelativeFile(String).
   */
  public boolean hasRelativeFile() {
    return file != null;
  }
  
  /**
   * Copies the directories from the given {@link FilePath} to this instance.
   * 
   * @param other another {@link FilePath}.
   * @return this instance.
   */
  public FilePath copyDirsFrom(FilePath other) {
    for (String d : other.dirs) {
      dirs.add(d);
    }
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Factory methods

  /**
   * Returns a {@link FilePath} corresponding to the path of a file (i.e.: not a directory).
   * 
   * @param pathLiteral the path of a file.
   * @return a new {@link FilePath}.
   */
  public static FilePath forFile(String pathLiteral) {
    StringTokenizer tk    = new StringTokenizer(pathLiteral, "/\\");
    List<String>    parts = new ArrayList<>();
    if (!pathLiteral.isEmpty() && (pathLiteral.charAt(0) == '\\' || pathLiteral.charAt(0) == '/')) {
      parts.add("");
    }
    while (tk.hasMoreTokens()) {
      String t = tk.nextToken();
      parts.add(t);
    }
    Assertions.notEmpty(parts, "Absolute path is empty");
   
    if (parts.size() == 1) {
      return FilePath.newInstance().setRelativeFile(parts.get(0));
    }
    
    FilePath path = FilePath.newInstance();
    for (int i = 0; i < parts.size() - 1; i++) {
      String p = parts.get(i);
      path.dirs.add(p);
    }
    path.setRelativeFile(parts.get(parts.size() - 1));
    return path;
  }
 
  /**
   * Returns a {@link FilePath} corresponding to the path of a directory (i.e.: not a file).
   * 
   * @param pathLiteral the path of a directory.
   * @return a new {@link FilePath}.
   */
  public static FilePath forDirectory(String pathLiteral) {
    StringTokenizer tk    = new StringTokenizer(pathLiteral, "/\\");
    FilePath        path  = FilePath.newInstance();
    if (!pathLiteral.isEmpty() && (pathLiteral.charAt(0) == '\\' || pathLiteral.charAt(0) == '/')) {
      path.dirs.add("");
    }
    while (tk.hasMoreTokens()) {
      path.dirs.add(tk.nextToken());
    }    
    return path;
  }
  
  /**
   * Adds the ${user.home} directory to this instance.
   * 
   * @return this instance.
   */
  public static FilePath forUserHome() {
    return FilePath.newInstance()
        .copyDirsFrom(FilePath.forDirectory(FileUtil.fixFileSeparators(System.getProperty("user.home"))));
  }
  
  /**
   * Adds the ${user.home}/.corus directory to this instance.
   * 
   * @return this instance.
   */
  public static FilePath forCorusUserDir() {
    return FilePath.newInstance()
        .copyDirsFrom(FilePath.forDirectory(FileUtil.fixFileSeparators(System.getProperty("user.home"))))
        .addDir(".corus");
  }
  
  /**
   * Adds the directory corresponding to the <code>java.io.tmpdir</code> system property.
   * 
   * @return this instance.
   */
  public static FilePath forJvmTempDir() {
    String tmp = System.getProperty("java.io.tmpdir");
    Assertions.illegalState(tmp == null, "java.io.tmpdir system property not set");
    return FilePath.newInstance().copyDirsFrom(FilePath.forDirectory(tmp));
  }
 
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return createFilePath();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FilePath) {
      FilePath other = (FilePath) obj;
      if (dirs.size() == other.dirs.size()) {
        for (int i = 0; i < dirs.size(); i++) {
          if (!dirs.get(i).equals(other.dirs.get(i))) {
            return false;
          }
        }
        if (ObjectUtil.safeEquals(file, other.file)) {
          return true;
        }
      }
      return false;
    } 
    return false;
  }
 
}
