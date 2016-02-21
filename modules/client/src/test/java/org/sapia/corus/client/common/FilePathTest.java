package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class FilePathTest {
  
  private FilePath path;
  
  @Before
  public void setUp() throws Exception {
    path = FilePath.newInstance();
  }

  @Test
  public void testAddDir_absolute_linux() {
    path.addDir("/test");
    assertEquals(2, path.length());
  }
  
  @Test
  public void testAddDir_absolute_windows() {
    path.addDir("c:/test");
    assertEquals(2, path.length());
  }
  
  @Test
  public void testAddDir_relative() {
    path.addDir("test");
    assertEquals(1, path.length());
  }
  
  @Test
  public void testAddUserHome() {
    path = FilePath.forUserHome();
    assertEquals(System.getProperty("user.home"), path.createFilePath());
  }

  @Test
  public void testAddCorusUserDir() {
    path = FilePath.forCorusUserDir();
    assertEquals(System.getProperty("user.home") + File.separator + ".corus", path.createFilePath());
  }

  @Test
  public void testAddJvmTempDir() {
    path = FilePath.forJvmTempDir();
    String expected = System.getProperty("java.io.tmpdir");
    if (expected.endsWith(File.separator)) {
      expected = expected.substring(0, expected.length() - 1);
    }
    assertEquals(expected, path.createFilePath());
  }

  @Test
  public void testGetDirectories() {
    path.addDir("d1").addDir("d2").addDir("d3");
    assertEquals("d1", path.getDirectories().get(0));
    assertEquals("d2", path.getDirectories().get(1));
    assertEquals("d3", path.getDirectories().get(2));
  }
  
  @Test
  public void testGetDirectoriesAsPath() {
    path.addDir("d1").addDir("d2").addDir("d3").setRelativeFile("test.txt");
    FilePath dirs = path.getDirectoriesAsPath();
    assertEquals("d1", dirs.getDirectories().get(0));
    assertEquals("d2", dirs.getDirectories().get(1));
    assertEquals("d3", dirs.getDirectories().get(2));
    assertFalse(dirs.hasRelativeFile());
  }

  @Test
  public void testSetRelativeFile() {
    FilePath fp = FilePath.newInstance();
    fp.setRelativeFile("test.txt");
    assertTrue(fp.hasRelativeFile());
  }

  @Test
  public void testCreateFileFrom() {
    File f = FilePath.newInstance().createFileFrom(new File(System.getProperty("user.dir")));
    assertEquals(System.getProperty("user.dir"), f.getAbsolutePath());
  }
  
  @Test
  public void testCreateFileFrom_with_relative_file() {
    File f = FilePath.newInstance().setRelativeFile("test.txt").createFileFrom(new File(System.getProperty("user.dir")));
    assertEquals(System.getProperty("user.dir") + File.separator + "test.txt", f.getAbsolutePath());
  }

  @Test
  public void testCreateFilePathFrom() {
    String p = FilePath.newInstance().createFilePathFrom(new File(System.getProperty("user.dir")));
    assertEquals(System.getProperty("user.dir"), p);
  }

  @Test
  public void testCreateFilePathFrom_with_relative_file() {
    String p = FilePath.newInstance().setRelativeFile("test.txt").createFilePathFrom(new File(System.getProperty("user.dir")));
    assertEquals(System.getProperty("user.dir") + File.separator + "test.txt", p);
  }
  
  @Test
  public void testCreateFile() {
    FilePath path = FilePath.newInstance().addDir("d1/d2/d3/d4");
    File     file = path.createFile();
    assertEquals("d1/d2/d3/d4".replace("/", File.separator), file.getPath());
  }

  @Test
  public void testCreateFilePath_for_file() {
    FilePath filePath = FilePath.newInstance().addDir("d1/d2/d3/d4").setRelativeFile("test.txt");
    assertEquals("d1/d2/d3/d4/test.txt".replace("/", File.separator), filePath.createFilePath());
  }
  
  @Test
  public void testCreateFilePath_for_absolute_file_linux() {
    FilePath filePath = FilePath.newInstance().addDir("/d1/d2/d3/d4").setRelativeFile("test.txt");
    assertEquals("/d1/d2/d3/d4/test.txt".replace("/", File.separator), filePath.createFilePath());
  }
  
  @Test
  public void testCreateFilePath_for_absolute_file_windows() {
    FilePath filePath = FilePath.newInstance().addDir("c:/d1/d2/d3/d4").setRelativeFile("test.txt");
    assertEquals("c:/d1/d2/d3/d4/test.txt".replace("/", File.separator), filePath.createFilePath());
  }
  
  @Test
  public void testCreateFilePath_for_file_not_directories() {
    FilePath filePath = FilePath.newInstance().setRelativeFile("test.txt");
    assertEquals("test.txt".replace("/", File.separator), filePath.createFilePath());
  }
  
  @Test
  public void testCreateFilePath_for_directory() {
    FilePath filePath = FilePath.newInstance().addDir("d1/d2/d3/d4");
    assertEquals("d1/d2/d3/d4".replace("/", File.separator), filePath.createFilePath());
  }


  @Test
  public void testGetDirCount() {
    FilePath filePath = FilePath.forDirectory("d1/d2/d3/d4");
    assertEquals(4, filePath.getDirCount());   
  }

  @Test
  public void testIsEmpty() {
    FilePath path = FilePath.newInstance().setRelativeFile("test.txt");
    assertTrue(path.isEmpty());
  }
  
  @Test
  public void testIsEmpty_false() {
    FilePath path = FilePath.newInstance().addDir("d1/d2/d3/d4").setRelativeFile("test.txt");
    assertFalse(path.isEmpty());
  }

  @Test
  public void testNotEmpty() {
    FilePath path = FilePath.newInstance().addDir("d1/d2/d3/d4").setRelativeFile("test.txt");
    assertTrue(path.notEmpty());
  }
  
  @Test
  public void testNotEmpty_false() {
    FilePath path = FilePath.newInstance().setRelativeFile("test.txt");
    assertFalse(path.notEmpty());
  }

  @Test
  public void testLength() {
    FilePath path = FilePath.forDirectory("d1/d2/d3/d4").setRelativeFile("test.txt");
    assertEquals(5, path.length());
  }
  
  @Test
  public void testLength_no_file() {
    FilePath path = FilePath.forDirectory("d1/d2/d3/d4");
    assertEquals(4, path.length());
  }

  @Test
  public void testLength_no_dirs() {
    FilePath path = FilePath.newInstance().setRelativeFile("test.txt");
    assertEquals(1, path.length());
  }

  @Test
  public void testHasFile() {
    FilePath path = FilePath.newInstance().addDir("d1").setRelativeFile("test.text");
    assertTrue(path.hasRelativeFile());
  }

  @Test
  public void testHasFile_false() {
    FilePath path = FilePath.newInstance().addDir("d1");
    assertFalse(path.hasRelativeFile());
  }
  
  @Test
  public void testForFile() {
    FilePath filePath = FilePath.forFile("d1/d2/d3/test.txt");
    assertTrue(filePath.hasRelativeFile());
    assertFalse(filePath.isAbsolute());
  }
  
  @Test
  public void testForFile_absolute() {
    FilePath filePath = FilePath.forFile("/d1/d2/d3/test.txt");
    assertTrue(filePath.hasRelativeFile());
    assertTrue(filePath.isAbsolute());
  }

  @Test
  public void testForDirectory() {
    FilePath dirPath = FilePath.forDirectory("d1/d2/d3");
    assertFalse(dirPath.hasRelativeFile());
    assertFalse(dirPath.isAbsolute());
  }
  
  @Test
  public void testForDirectory_absolute() {
    FilePath dirPath = FilePath.forDirectory("/d1/d2/d3");
    assertFalse(dirPath.hasRelativeFile());
    assertTrue(dirPath.isAbsolute());
  }

}
