package org.sapia.corus.deployer.archiver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.deployer.archiver.DistributionArchiver.DistributionArchive;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class DistributionArchiverImplTest {
  
  @Mock
  private FileSystemModule      fileSystem;
  @Mock
  private DeployerConfiguration conf;
  @Mock
  private File archiveDir, anyFile;
  
  private DistributionArchiverImpl archiver;
  
  
  @Before
  public void setUp() {
    archiver = new DistributionArchiverImpl();
    archiver.setDeployerConf(conf);
    archiver.setFileSystem(fileSystem);
    
    when(anyFile.mkdirs()).thenReturn(true);
    when(anyFile.getAbsolutePath()).thenReturn("testAbsolutePath");
    when(fileSystem.getFileHandle("testArchiveDir")).thenReturn(archiveDir);
    when(fileSystem.getFileHandle(anyString())).thenReturn(anyFile);
    when(fileSystem.exists(any(File.class))).thenReturn(true);
    when(conf.getArchiveDir()).thenReturn("testArchiveDir");
    when(conf.getDeployDir()).thenReturn("testDeployDir");
  }

  @Test
  public void testArchive() throws IOException {
    Distribution d = new Distribution("dist", "1.0");
    archiver.archive(RevId.valueOf("test"), Collects.arrayToList(d));
    
    verify(fileSystem).zipDirectory(any(File.class), eq(true), any(File.class));
  }

  @Test
  public void testUnarchive() throws IOException {
    File mockZip = mock(File.class);
    
    when(mockZip.getName()).thenReturn("test.zip");
    when(anyFile.listFiles(any(FileFilter.class))).thenReturn(new File[] { mockZip });
    
    List<DistributionArchive> archives = archiver.unarchive(RevId.valueOf("test"));
    assertEquals(1, archives.size());
    assertEquals(mockZip, archives.get(0).getDistributionZip());
  }
}
