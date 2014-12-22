package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.FileUtils.FileInfo;
import org.sapia.ubik.util.Collects;

public class FileUtilsTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testIsAbsolute() {
    assertTrue(FileUtils.isAbsolute("/this/is/absolute"));
    assertTrue(FileUtils.isAbsolute("c:/this/is/absolute"));
    assertFalse(FileUtils.isAbsolute("this/is/relative"));
  }

  @Test
  public void testIsPathSeparator() {
    assertTrue(FileUtils.isPathSeparator('/'));
    assertTrue(FileUtils.isPathSeparator('\\'));
  }

  @Test
  public void testIsWindowsDrive() {
    assertTrue(FileUtils.isWindowsDrive("c:"));
    assertTrue(FileUtils.isWindowsDrive("E:"));
    assertFalse(FileUtils.isWindowsDrive("C"));
  }

  @Test
  public void testGetFileInfoForDirectory() {
    FileInfo info = FileUtils.getFileInfo("/some/directory");
    assertEquals("/some/directory", info.directory);
    assertTrue(info.fileName == null);
  }

  @Test
  public void testGetFileInfoForFile() {
    FileInfo info = FileUtils.getFileInfo("/some/directory/file.txt");
    assertEquals("/some/directory", info.directory);
    assertEquals("file.txt", info.fileName);
  }

  @Test
  public void testIsClassesDirectory() {
    FileInfo info = FileUtils.getFileInfo("/some/classes/");
    assertEquals("/some/classes/", info.directory);
    assertTrue("Should be flagged as classes directory", info.isClasses);
    assertTrue(info.fileName == null);
  }
  
  @Test
  public void testSplitFilePaths() {
    Set<String> paths = Collects.arrayToSet(FileUtils.splitFilePaths("path0:path1;path2"));
    assertEquals(3, paths.size());
    for (int i = 0; i < 3; i++) {
      assertTrue("Expected path" + i , paths.contains("path" + i));
    }
  }
  
  @Test
  public void testAppendWithSeparatorInPathAndSubPath() {
    assertEquals("path" + File.separator + "subPath", FileUtils.append("path/", "/subPath"));
  }

  @Test
  public void testAppendWithSeparatorInPath() {
    assertEquals("path" + File.separator + "subPath", FileUtils.append("path/", "subPath"));
  }

  @Test
  public void testAppendWithSeparatorInSubPath() {
    assertEquals("path" + File.separator + "subPath", FileUtils.append("path", "/subPath"));
  }

  @Test
  public void testAppendWithNullSubPath() {
    assertEquals("path", FileUtils.append("path", null));
  }

  @Test
  public void testAppendWithEmptySubPath() {
    assertEquals("path", FileUtils.append("path", ""));
  }

  @Test
  public void testAppendWithSeparatorSubPath() {
    assertEquals("path", FileUtils.append("path", "/"));
  }

  @Test
  public void testAppendWithWindowsSeparatorSubPath() {
    assertEquals("path", FileUtils.append("path", "\\"));
  }

  @Test
  public void testToPathNullArray() {
    assertEquals("", FileUtils.toPath(null));
  }

  @Test
  public void testToPathSingleElement() {
    assertEquals("path", FileUtils.toPath("path"));
  }

  @Test
  public void testToPathMultipleElements() {
    assertEquals("path1" + File.separator + "path2", FileUtils.toPath("path1", "path2"));
  }

}
