package org.sapia.corus.client.services.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstracts the file system.
 * 
 * @author yduchesne
 *
 */
public interface FileSystemModule{
  
  public static final String ROLE = FileSystemModule.class.getName();

  /**
   * Performs recursive deletion of the given directory.
    *
   * @param dir a {@link File} corresponding to a directory to delete.
   * @throws IOException if the operation failed.
   */
  public void deleteDirectory(File dir) throws IOException;
  
  /**
   * Deletes the given file.
   * 
   * @param file the {@link File} to delete.
   * @throws IOException if the operation failed.
   */
  public void deleteFile(File file) throws IOException;

  /**
   * Creates the directory corresponding to the given file object (supports nested paths).
   * 
   * @param dir a {@link File} corresponding to a directory to create.
   * @throws IOException if the operation failed.
   */
  public void createDirectory(File dir) throws IOException;
  
  /**
   * This method checks for the existence of a given file or directory. Use instead of {@link File#exists()},
   * so that the existence check can be mocked as part of unit tests.
   * 
   * @param toCheck a {@link File}
   * @return <code>true</code> if the file or directory corresponding to the passed {@link File} object exists.
   */
  public boolean exists(File toCheck);
  
  /**
   * Unzips a given file.
   * 
   * @param toUnzip the {@link File} to unzip.
   * @param destDir the {@link File} corresponding to the directory where the content of the zip file should be extracted.
   * @throws IOException a problem occurs while unzipping the file.
   */
  public void unzip(File toUnzip, File destDir) throws IOException;
  
  
  /**
   * Opens a stream from a given zip file and returns it.
   *  
   * @param zipFile the {@link File} corresponding to the zip file to read from.
   * @param entryName the name of the entry whose corresponding stream should be returned.
   * @return the {@link InputStream} corresponding to the desired entry.
   * @throws IOException if a problem occurs attempting to open the entry stream. 
   */
  public InputStream openZipEntryStream(File zipFile, String entryName) throws IOException;
}
