package org.sapia.corus.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.sapia.corus.client.services.file.FileSystemModule;

public class TestFileSystemModule implements FileSystemModule{
  
  @Override
  public void createDirectory(File dir) throws IOException {
  }
  
  @Override
  public void deleteDirectory(File dir) throws IOException {
  }
  
  @Override
  public void deleteFile(File file) throws IOException {
  }
  
  @Override
  public boolean exists(File toCheck) {
    return true;
  }

  @Override
  public InputStream openZipEntryStream(File zipFile, String entryName)
      throws IOException {
    return null;
  }
  
  @Override
  public void unzip(File doUnzip, File destDir) throws IOException {
  }
  
  @Override
  public void zipDirectory(File destFile, boolean isRecursive, File srcDir) throws IOException {
  }
  
  @Override
  public void zipFile(File srcFile, File destFile) throws IOException {
  }
  
  @Override
  public List<File> listFiles(File baseDir) {
    return new ArrayList<File>();
  }
  
  @Override
  public File getFileHandle(String path) {
    return new File(path);
  }
  
  @Override
  public Reader getFileReader(File f) throws FileNotFoundException, IOException {
    return new StringReader("test");
  }
  
  @Override
  public OutputStream getFileOutputStream(File f) throws IOException {
    return new ByteArrayOutputStream();
  }
  
  @Override
  public void renameDirectory(File from, File to) throws IOException {
  }
  
}
