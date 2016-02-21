package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.FileUtil.FileInfo;
import org.sapia.ubik.util.Collects;

public class FileUtilTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testIsAbsolute() {
    assertTrue(FileUtil.isAbsolute("/this/is/absolute"));
    assertTrue(FileUtil.isAbsolute("c:/this/is/absolute"));
    assertFalse(FileUtil.isAbsolute("this/is/relative"));
  }

  @Test
  public void testIsPathSeparator() {
    assertTrue(FileUtil.isPathSeparator('/'));
    assertTrue(FileUtil.isPathSeparator('\\'));
  }

  @Test
  public void testIsWindowsDrive() {
    assertTrue(FileUtil.isWindowsDrive("c:"));
    assertTrue(FileUtil.isWindowsDrive("E:"));
    assertFalse(FileUtil.isWindowsDrive("C"));
  }

  @Test
  public void testGetFileInfoForDirectory() {
    FileInfo info = FileUtil.getFileInfo("/some/directory");
    assertEquals("/some/directory", info.directory);
    assertTrue(info.fileName == null);
  }

  @Test
  public void testGetFileInfoForFile() {
    FileInfo info = FileUtil.getFileInfo("/some/directory/file.txt");
    assertEquals("/some/directory", info.directory);
    assertEquals("file.txt", info.fileName);
  }

  @Test
  public void testIsClassesDirectory() {
    FileInfo info = FileUtil.getFileInfo("/some/classes/");
    assertEquals("/some/classes/", info.directory);
    assertTrue("Should be flagged as classes directory", info.isClasses);
    assertTrue(info.fileName == null);
  }
  
  @Test
  public void testSplitFilePaths() {
    Set<String> paths = Collects.arrayToSet(FileUtil.splitFilePaths("path0:path1;path2"));
    assertEquals(3, paths.size());
    for (int i = 0; i < 3; i++) {
      assertTrue("Expected path" + i , paths.contains("path" + i));
    }
  }
  
  @Test
  public void testSplitFilePaths_with_single_windows_path() {
    Set<String> paths = Collects.arrayToSet(FileUtil.splitFilePaths("E:\\path\\bar"));
    assertEquals(1, paths.size());
  }
  
  @Test
  public void testSplitFilePaths_with_windows_path() {
    String[] paths = FileUtil.splitFilePaths("path0:path1;path2;E:\\path3");
  
    assertEquals(4, paths.length);
    assertEquals("path0", paths[0]);
    assertEquals("path1", paths[1]);
    assertEquals("path2", paths[2]);
    assertEquals("E:\\path3", paths[3]);
  }
  
  @Test
  public void testSplitFilePaths_with_multiple_windows_paths() {
    String[] paths = FileUtil.splitFilePaths("path0:path1:C:\\path2;E:\\path3");
  
    assertEquals(4, paths.length);
    assertEquals("path0", paths[0]);
    assertEquals("path1", paths[1]);
    assertEquals("C:\\path2", paths[2]);
    assertEquals("E:\\path3", paths[3]);
  }
  
  @Test
  public void testAppendWithSeparatorInPathAndSubPath() {
    assertEquals("path" + File.separator + "subPath", FileUtil.append("path" + File.separator, File.separator + "subPath"));
  }

  @Test
  public void testAppendWithSeparatorInPath() {
    assertEquals("path" + File.separator + "subPath", FileUtil.append("path" + File.separator, "subPath"));
  }

  @Test
  public void testAppendWithSeparatorInSubPath() {
    assertEquals("path" + File.separator + "subPath", FileUtil.append("path", File.separator +  "subPath"));
  }

  @Test
  public void testAppendWithNullSubPath() {
    assertEquals("path", FileUtil.append("path", null));
  }

  @Test
  public void testAppendWithEmptySubPath() {
    assertEquals("path", FileUtil.append("path", ""));
  }

  @Test
  public void testAppendWithSeparatorSubPath() {
    assertEquals("path", FileUtil.append("path", "/"));
  }

  @Test
  public void testAppendWithWindowsSeparatorSubPath() {
    assertEquals("path", FileUtil.append("path", "\\"));
  }

  @Test
  public void testToPathNullArray() {
    assertEquals("", FileUtil.toPath(null));
  }

  @Test
  public void testToPathSingleElement() {
    assertEquals("path", FileUtil.toPath("path"));
  }

  @Test
  public void testToPathMultipleElements() {
    assertEquals("path1" + File.separator + "path2", FileUtil.toPath("path1", "path2"));
  }
  
  @Test
  public void testFixFileSeparators() {
    char platformSep = File.separatorChar;
    char wrongSep = platformSep == '\\' ? '/' : '\\';
    
    String wrongPath = "p1" + wrongSep + "p2" + wrongSep + "p3";
    String newPath = FileUtil.fixFileSeparators(wrongPath);
    assertFalse(newPath.contains("" + wrongSep));
    assertTrue(newPath.contains("" + platformSep));
  }

}
