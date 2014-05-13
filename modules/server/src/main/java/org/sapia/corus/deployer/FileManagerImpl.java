package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.util.FilePath;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link FileManager} interface.
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = { FileManager.class, InternalFileManager.class })
@Remote(interfaces = { FileManager.class })
public class FileManagerImpl extends ModuleHelper implements InternalFileManager {

  @Autowired
  private FileSystemModule fileSystem;

  @Autowired
  private DeployerConfiguration deployerConfig;

  private File baseDir;

  // --------------------------------------------------------------------------
  // Provided for testing.

  public final void setFileSystem(FileSystemModule fileSystem) {
    this.fileSystem = fileSystem;
  }

  public final void setDeployerConfig(DeployerConfiguration deployerConfig) {
    this.deployerConfig = deployerConfig;
  }

  // --------------------------------------------------------------------------
  // Module interface

  @Override
  public String getRoleName() {
    return FileManager.ROLE;
  }

  // --------------------------------------------------------------------------
  // Lifecycle

  @Override
  public void init() throws Exception {
  }

  @Override
  public void start() throws Exception {
    baseDir = FilePath.newInstance().addDir(deployerConfig.getUploadDir()).createFile();
  }

  @Override
  public void dispose() throws Exception {
  }

  // --------------------------------------------------------------------------
  // FileManager interface

  @Override
  public ProgressQueue deleteFiles(final FileCriteria criteria) {

    ProgressQueue progress = new ProgressQueueImpl();

    List<File> toDelete = IteratorFilter.newFilter(new Matcher<File>() {
      @Override
      public boolean matches(File file) {
        return criteria.getName().matches(file.getName());
      }
    }).filter(fileSystem.listFiles(baseDir).iterator()).get();

    for (File f : toDelete) {
      if (!f.delete()) {
        progress.info("Could not delete: " + f.getName());
      } else {
        progress.info("Deleted: " + f.getName());
      }
    }

    progress.close();
    return progress;
  }

  @Override
  public List<FileInfo> getFiles() {
    List<FileInfo> files = Collects.convertAsList(fileSystem.listFiles(baseDir), new Func<FileInfo, File>() {
      public FileInfo call(File file) {
        return new FileInfo(file.getName(), file.length(), new Date(file.lastModified()));
      }
    });
    Collections.sort(files, new FileInfoComparator());
    return files;
  }

  @Override
  public List<FileInfo> getFiles(final FileCriteria criteria) {
    return IteratorFilter.newFilter(new Matcher<FileInfo>() {
      @Override
      public boolean matches(FileInfo file) {
        return criteria.getName().matches(file.getName());
      }
    }).filter(getFiles().iterator()).get();
  }

  @Override
  public File getFile(FileInfo info) throws FileNotFoundException {
    File toReturn = new File(baseDir, info.getName());
    if (!fileSystem.exists(toReturn)) {
      throw new FileNotFoundException("File not found: " + toReturn.getAbsolutePath());
    }
    return toReturn;
  }

  // ==========================================================================

  private static class FileInfoComparator implements Comparator<FileInfo> {

    @Override
    public int compare(FileInfo o1, FileInfo o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
}
