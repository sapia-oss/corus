package org.sapia.corus.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.TypeSelector;
import org.apache.tools.ant.types.selectors.TypeSelector.FileType;
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
    if (dir.exists()) {
      if (!dir.isDirectory()) {
        throw new IllegalArgumentException(String.format("Not a directory: %s", dir.getAbsolutePath()));
      }
      try {
        FileUtils.deleteDirectory(dir);
      } catch (IOException e) {
        if (dir.exists()) {
          throw e;
        } 
      }
    }
  }

  @Override
  public void deleteFile(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IllegalArgumentException(String.format("File is a directory directory: %s", file.getAbsolutePath()));
      }
      file.delete();
    }
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
  public void zipDirectory(File srcDir, boolean isRecursive, File destFile) throws IOException {
    Assert.isTrue(srcDir.isDirectory(), "Directory to zip is not a directory: " + srcDir.getAbsolutePath());

    Zip zip = new Zip();
    zip.setDestFile(destFile);
    
    if (isRecursive) {
      zip.setBasedir(srcDir);
    } else {
      TypeSelector fileSelector = new TypeSelector();
      fileSelector.setType((FileType) EnumeratedAttribute.getInstance(FileType.class, FileType.FILE));

      FileSet fs = new FileSet();
      fs.setDir(srcDir);
      fs.setIncludes("*");
      fs.addType(fileSelector);
      
      zip.addFileset(fs);
    }
    
    zip.setProject(new Project());
    try {
      zip.execute();
    } catch (BuildException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
  
  @Override
  public void zipFile(File srcFile, File destFile) throws IOException {
    Assert.isTrue(srcFile.isFile(), "File to zip is not a file: " + srcFile.getAbsolutePath());

    Zip zip = new Zip();
    zip.setDestFile(destFile);
    FileSet fs = new FileSet();
    fs.setDir(srcFile.getParentFile());
    fs.setIncludes(srcFile.getName());
    zip.addFileset(fs);
    
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
  
  @Override
  public File getFileHandle(String path) {
    return new File(path);
  }
  
  @Override
  public Reader getFileReader(File f) throws FileNotFoundException, IOException {
    return new FileReader(f);
  }
  
  @Override
  public void renameDirectory(File from, File to) throws IOException {
    FileUtils.copyDirectory(from, to);
  }
}
