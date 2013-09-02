package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathUtilsTest {
  
  @Test
  public void testAppendWithSeparatorInPathAndSubPath() {
    assertEquals("path/subPath", PathUtils.append("path/", "/subPath"));
  }
  
  @Test
  public void testAppendWithSeparatorInPath() {
    assertEquals("path/subPath", PathUtils.append("path/", "subPath"));
  }
  
  @Test
  public void testAppendWithSeparatorInSubPath() {
    assertEquals("path/subPath", PathUtils.append("path", "/subPath"));
  }
  
  @Test
  public void testAppendWithNullSubPath() {
    assertEquals("path", PathUtils.append("path", null));
  }
  
  @Test
  public void testAppendWithEmptySubPath() {
    assertEquals("path", PathUtils.append("path", ""));
  }
  
  @Test
  public void testAppendWithSeparatorSubPath() {
    assertEquals("path", PathUtils.append("path", "/"));
  }
  
  @Test
  public void testAppendWithWindowsSeparatorSubPath() {
    assertEquals("path", PathUtils.append("path", "\\"));
  }

  @Test
  public void testToPathNullArray() {
    assertEquals("", PathUtils.toPath(null));
  }
  
  @Test
  public void testToPathSingleElement() {
    assertEquals("path", PathUtils.toPath("path"));
  }
  
  @Test
  public void testToPathMultipleElements() {
    assertEquals("path1/path2", PathUtils.toPath("path1", "path2"));
  }
  
  
}
