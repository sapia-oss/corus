package org.sapia.corus.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ZipUtils;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.core.ModuleHelper;
import org.springframework.util.Assert;

/**
 * Implementation of the {@link FileSystemModule} interface.
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = FileSystemModule.class)
public class FileSystemModuleImpl extends ModuleHelper implements FileSystemModule {

  private static final int ZIP_READ_CAPACITY = 2048;

  @Override
  public String getRoleName() {
    return FileSystemModule.ROLE;
  }

  @Override
  public void init() throws Exception {
  }

  @Override
  public void dispose() throws Exception {
  }

  // ///////////// FileSystemModule interface methods ////////////////

  @Override
  public void createDirectory(File dir) {
    dir.mkdirs();
  }

  @Override
  public void deleteDirectory(File dir) throws IOException {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(String.format("Not a directory: %s", dir.getAbsolutePath()));
    }
    FileUtils.deleteDirectory(dir);
  }

  @Override
  public void deleteFile(File file) throws IOException {
    if (file.isDirectory()) {
      throw new IllegalArgumentException(String.format("File is a directory directory: %s", file.getAbsolutePath()));
    }
    file.delete();
  }

  @Override
  public boolean exists(File toCheck) {
    return toCheck.exists();
  }

  @Override
  public InputStream openZipEntryStream(File zipFile, String entryName) throws IOException {
    return ZipUtils.readEntryStream(zipFile.getAbsolutePath(), entryName, ZIP_READ_CAPACITY, ZIP_READ_CAPACITY);
  }

  @Override
  public void unzip(File toUnzip, File destDir) throws IOException {
    Assert.isTrue(!toUnzip.isDirectory(), "File to unzip is in fact a directory: " + toUnzip.getAbsolutePath());
    Assert.isTrue(destDir.isDirectory(), "Destination directory is a file: " + destDir.getAbsolutePath());

    Expand unzip = new Expand();
    unzip.setSrc(toUnzip);
    unzip.setDest(destDir);
    unzip.setProject(new Project());
    try {
      unzip.execute();
    } catch (BuildException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  @Override
  public void zip(File srcDir, File destFile) throws IOException {
    Assert.isTrue(srcDir.isDirectory(), "Directory to zip is not a directory: " + srcDir.getAbsolutePath());

    Zip zip = new Zip();
    zip.setBasedir(srcDir);
    zip.setDestFile(destFile);
    zip.setProject(new Project());
    try {
      zip.execute();
    } catch (BuildException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  @Override
  public List<File> listFiles(File baseDir) {
    File[] files = baseDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return !f.isDirectory() && !f.isHidden();
      }
    });

    if (files == null) {
      return new ArrayList<File>(0);
    }

    return Arrays.asList(files);
  }
}
