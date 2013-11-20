package org.sapia.corus.http.filesystem;

import java.io.File;

/**
 * Descriptor object of a file for the {@link FileSystemExtension}.
 * 
 * @author jcdesrochers
 */
public class FileEntry {

  /**
   * Factory method that creates a new {@link FileEntry} instance for a given
   * {@link File}.
   * 
   * @param aFile
   *          The {@link File} for this entry.
   * @return The created instance.
   */
  public static FileEntry createNew(File aFile) {
    FileEntry created = new FileEntry(aFile.getName(), false, aFile);
    return created;
  }

  /**
   * Factory method that creates a new {@link FileEntry} instance as a symbolic
   * link.
   * 
   * @param aName
   *          The name of the file entry.
   * @param aFile
   *          The {@link File} for this entry.
   * @return The created instance.
   */
  public static FileEntry createNewLink(String aName, File aFile) {
    FileEntry created = new FileEntry(aName, true, aFile);
    return created;
  }

  /** The name of this file entry. */
  private String name;

  /** Indicates if this entry is a symbolic link or not, */
  private boolean isLink;

  /** The underlying File to acces the file. */
  private File file;

  /**
   * Creates a new {@link FileEntry} instance with the arguments passed in.
   * 
   * @param aName
   *          The name of this file entry.
   * @param isLink
   *          True if this is a link, false otherwise.
   * @param aFile
   *          The underlying {@link File} of this entry.
   */
  protected FileEntry(String aName, boolean isLink, File aFile) {
    name = aName;
    this.isLink = isLink;
    file = aFile;
  }

  /**
   * @return The name of this file entry.
   */
  public String getName() {
    return name;
  }

  /**
   * @return True if this is a directory, false otherwise.
   */
  public boolean isDirectory() {
    return (file != null && file.isDirectory());
  }

  /**
   * @return True if this is a symblic link, false otherwise.
   */
  public boolean isLink() {
    return isLink;
  }

  /**
   * @return The underlying {@link File} of this entry.
   */
  public File getFile() {
    return file;
  }

}
