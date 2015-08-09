package org.sapia.corus.deployer.archiver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.core.ModuleHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the {@link DistributionArchiver} interface.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = { DistributionArchiver.class })
public class DistributionArchiverImpl extends ModuleHelper implements DistributionArchiver {
  
  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  
  @Autowired
  private FileSystemModule      fileSystem;
 
  @Autowired
  private DeployerConfiguration deployerConf;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setDeployerConf(DeployerConfiguration deployerConf) {
    this.deployerConf = deployerConf;
  }
  
  void setFileSystem(FileSystemModule fileSystem) {
    this.fileSystem = fileSystem;
  }
  
  // --------------------------------------------------------------------------
  // Module
  
  public String getRoleName() {
    return ROLE;
  }
  
  @Override
  public void init() throws Exception {
  }
  
  @Override
  public void start() throws Exception {
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // DistributionArchiver interface
  
  @Override
  public void archive(RevId revId, List<Distribution> toArchive) throws IOException {
    log.debug("Archiving distributions for rev: " + revId);
    File revDir = fileSystem.getFileHandle(
        FilePath.newInstance()
          .addDir(deployerConf.getArchiveDir())
          .addDir(revId.get()).createFilePath()
    );
    
    if (revDir.exists()) {
      FileUtils.deleteDirectory(revDir);
    }
    
    if (!revDir.mkdirs()) {
      throw new IOException("Could not make revision directory: " + revDir.getAbsolutePath());
    } 
    
    for (Distribution d : toArchive) {
      log.debug("Archiving: " + d);
      File distDir = fileSystem.getFileHandle(
          FilePath.newInstance()
            .addDir(deployerConf.getDeployDir())
            .addDir(d.getName())
            .addDir(d.getVersion())
            .addDir("common").createFilePath()
      );
      File distZip = fileSystem.getFileHandle(
          FilePath.newInstance()
            .addDir(revDir.getAbsolutePath())
            .setRelativeFile(d.getName() + "-" + d.getVersion() + ".zip")
            .createFilePath()
      );
      fileSystem.zipDirectory(distDir, true, distZip);
    }
  }
  
  @Override
  public List<DistributionArchive> unarchive(RevId revId) throws IOException {
    log.debug("Unarchiving distributions for revision: " + revId);

    final File revDir = fileSystem.getFileHandle(
        FilePath.newInstance()
          .addDir(deployerConf.getArchiveDir())
          .addDir(revId.get()).createFilePath()
    );
    
    List<DistributionArchive> toReturn = new ArrayList<>();
    
    if (!fileSystem.exists(revDir)) {
      return toReturn;
    } 

    
    for (final File zip : revDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".zip");
      }
    })) {
      toReturn.add(new DistributionArchive() {
        @Override
        public File getDistributionZip() {
          return zip;
        }
      });
    }
    
    return toReturn;
  }
  
  @Override
  public void clear(RevId revId) throws IOException {
    log.debug("Clearing archived distributions for revision: " + revId);

    File revDir = fileSystem.getFileHandle(
        FilePath.newInstance()
          .addDir(deployerConf.getArchiveDir())
          .addDir(revId.get())
          .createFilePath()
    );
    
    if (revDir.exists()) {
      log.debug("Clearing revision directory: " + revDir);
      FileUtils.deleteDirectory(revDir);
      revDir.delete();
    }
  }
  
  @Override
  public void clear() throws IOException {
    log.debug("Clearing archived distributions for all revisions");
    
    File toClear = fileSystem.getFileHandle(
        FilePath.newInstance()
          .addDir(deployerConf.getArchiveDir())
          .createFilePath()
    );
    
    if (toClear.exists()) {
      for (File revId : toClear.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      })) {
        log.debug("Clearing rev dir: " + revId.getAbsolutePath());
        FileUtils.deleteDirectory(revId);
        revId.delete();
      }
    }
  }
  
}
