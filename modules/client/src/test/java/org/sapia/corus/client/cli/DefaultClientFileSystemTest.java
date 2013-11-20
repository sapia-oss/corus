package org.sapia.corus.client.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultClientFileSystemTest {

  @Test
  public void testIsAbsoluteWindowsPath() {
    assertTrue(DefaultClientFileSystem.isAbsolute("c:/foo/bar"));
  }

  @Test
  public void testIsAbsoluteUnixPath() {
    assertTrue(DefaultClientFileSystem.isAbsolute("/foo/bar"));
  }

  @Test
  public void testRelativePath() {
    assertFalse(DefaultClientFileSystem.isAbsolute("foo/bar"));
  }
}
