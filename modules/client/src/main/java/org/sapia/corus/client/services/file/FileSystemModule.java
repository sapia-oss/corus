package org.sapia.corus.client.services.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

import org.sapia.corus.client.common.FileFacade;

/**
 * Abstracts the file system.
 * 
 * @author yduchesne
 * 
 */
public interface FileSystemModule {

  public static final String ROLE = FileSystemModule.class.getName();

  /**
   * Performs recursive deletion of the given directory.
   * 
   * @param dir
   *          a {@link File} corresponding to a directory to delete.
   * @throws IOException
   *           if the operation failed.
   */
  public void deleteDirectory(File dir) throws IOException;

  /**
   * Deletes the given file.
   * 
   * @param file
   *          the {@link File} to delete.
   * @throws IOException
   *           if the operation failed.
   */
  public void deleteFile(File file) throws IOException;

  /**
   * Creates the directory corresponding to the given file object (supports
   * nested paths).
   * 
   * @param dir
   *          a {@link File} corresponding to a directory to create.
   * @throws IOException
   *           if the operation failed.
   */
  public void createDirectory(File dir) throws IOException;

  /**
   * This method checks for the existence of a given file or directory. Use
   * instead of {@link File#exists()}, so that the existence check can be mocked
   * as part of unit tests.
   * 
   * @param toCheck
   *          a {@link File}
   * @return <code>true</code> if the file or directory corresponding to the
   *         passed {@link File} object exists.
   */
  public boolean exists(File toCheck);

  /**
   * Unzips a given file.
   * 
   * @param toUnzip
   *          the {@link File} to unzip.
   * @param destDir
   *          the {@link File} corresponding to the directory where the content
   *          of the zip file should be extracted.
   * @throws IOException
   *           a problem occurs while unzipping the file.
   */
  public void unzip(File toUnzip, File destDir) throws IOException;

  /**
   * Zips a given directory.
   * 
   * @param srcDir The {@link File} corresponding to the directory to zip.
   * @param isRecursive Flag to include (or not) the content of sub-directories.
   * @param destFile The zip {@link File} to generate.
   * @throws IOException If a problem occurs while generating the zip file.
   */
  public void zipDirectory(File srcDir, boolean isRecursive, File destFile) throws IOException;

  /**
   * Zips a given file
   * 
   * @param srcFile The {@link File} corresponding to the file to zip.
   * @param destFile The zip {@link File} to generate.
   * @throws IOException If a problem occurs while generating the zip file.
   */
  public void zipFile(File srcFile, File destFile) throws IOException;

  /**
   * Opens a stream from a given zip file and returns it.
   * 
   * @param zipFile
   *          the {@link File} corresponding to the zip file to read from.
   * @param entryName
   *          the name of the entry whose corresponding stream should be
   *          returned.
   * @return the {@link InputStream} corresponding to the desired entry.
   * @throws IOException
   *           if a problem occurs attempting to open the entry stream.
   */
  public InputStream openZipEntryStream(File zipFile, String entryName) throws IOException;
 
  /**
   * @param baseDir
   *          the {@link File} corresponding to the directory from which to get
   *          the file list.
   * @return the {@link List} of files under the given base directory (does not
   *         return files that correspond to subdirectories).
   */
  public List<File> listFiles(File baseDir);
  
  /**
   * @param path the path of the {@link File} handle to return.
   * @return a new {@link File} handle, for the given path.
   */
  public File getFileHandle(String path);
  
  /**
   * @param toWrap a {@link File} to wrap into a {@link FileFacade}.
   * @return the {@link FileFacade} wrapping the given file.
   */
  public FileFacade getFileFacade(File toWrap);
  
  /**
   * @param a {@link File} for which to return a reader.
   * @return a new {@link Reader} for the given file.
   * 
   * @throws FileNotFoundException if the given file does not exist.
   * @throws IOException if a low-level I/O error occurred.
   */
  public Reader getFileReader(File f) throws FileNotFoundException, IOException;
  
  /**
   * @param f a {@link File} for which to return an {@link OutputStream}.
   * @return a new {@link OutputStream} allowing to write to the given file.
   * @throws IOException if an I/O error occurs while trying to open the stream.
   */
  public OutputStream getFileOutputStream(File f) throws IOException;
  
  /**
   * @param from the {@link File} corresponding to the directory to rename.
   * @param to the {@link File} corresponding to the target directory.
   * @throws IOException if an I/O error occurred while performing this operation.
   */
  public void renameDirectory(File from, File to) throws IOException;
}
