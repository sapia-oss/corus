package org.sapia.corus.deployer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class FileManagerImplTest {
  
  @Mock
  private FileSystemModule fileSystem;

  @Mock
  private DeployerConfiguration config;
  
  private FileManagerImpl manager;
  
  @Before
  public void setUp() {
    manager = new FileManagerImpl();
    manager.setFileSystem(fileSystem);
    manager.setDeployerConfig(config);
    
    when(config.getUploadDir()).thenReturn("uploadDir");
  }

  @Test
  public void testDeleteFile() {
    File file1 = mock(File.class);
    when(file1.getName()).thenReturn("file10");
    when(file1.length()).thenReturn(100L);
    
    File file2 = mock(File.class);
    when(file2.getName()).thenReturn("file20");
    when(file2.length()).thenReturn(1000L);    
    
    List<File> files = Collects.arrayToList(file2, file1);
    
    when(fileSystem.listFiles(any(File.class))).thenReturn(files);
    
    manager.deleteFiles(FileCriteria.newInstance().setName(ArgMatchers.parse("file1*")));
    
    verify(file1).delete();
  }

  @Test
  public void testGetFiles() {
    File file1 = mock(File.class);
    when(file1.getName()).thenReturn("file1");
    when(file1.length()).thenReturn(100L);
    
    File file2 = mock(File.class);
    when(file2.getName()).thenReturn("file2");
    when(file2.length()).thenReturn(1000L);    
    
    List<File> files = Collects.arrayToList(file2, file1);
    
    when(fileSystem.listFiles(any(File.class))).thenReturn(files);
    
    List<FileInfo> fileInfos = manager.getFiles();
    
    assertEquals(2, fileInfos.size());
    
    assertEquals("file1", fileInfos.get(0).getName());
    assertEquals(100L, fileInfos.get(0).getLength());

    assertEquals("file2", fileInfos.get(1).getName());
    assertEquals(1000L, fileInfos.get(1).getLength());
    
  }

  @Test
  public void testGetFilesFileCriteria() {
    File file1 = mock(File.class);
    when(file1.getName()).thenReturn("file10");
    when(file1.length()).thenReturn(100L);
    
    File file2 = mock(File.class);
    when(file2.getName()).thenReturn("file20");
    when(file2.length()).thenReturn(1000L);    
    
    List<File> files = Collects.arrayToList(file2, file1);
    
    when(fileSystem.listFiles(any(File.class))).thenReturn(files);
    
    List<FileInfo> fileInfos = manager.getFiles(FileCriteria.newInstance().setName(ArgMatchers.parse("file1*")));
    
    assertEquals(1, fileInfos.size());
    
    assertEquals("file10", fileInfos.get(0).getName());
  }

}


