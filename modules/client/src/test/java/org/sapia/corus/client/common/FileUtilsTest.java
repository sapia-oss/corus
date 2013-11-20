package org.sapia.corus.client.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.FileUtils.FileInfo;

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

}
