package org.sapia.corus.sigar;

import static org.junit.Assert.*;

import java.util.StringTokenizer;

import org.junit.Test;

public class SigarModuleImplTest {

  @Test
  public void testTokenizeLibPath_windows() {
    StringTokenizer tk = SigarModuleImpl.tokenizeLibPath("d:/path/1;d:/path/2", ";");
    assertEquals("d:/path/1", tk.nextToken());
    assertEquals("d:/path/2", tk.nextToken());
  }

  @Test
  public void testTokenizeLibPath_linux() {
    StringTokenizer tk = SigarModuleImpl.tokenizeLibPath("/path/1:/path/2", ":");
    assertEquals("/path/1", tk.nextToken());
    assertEquals("/path/2", tk.nextToken());
  }
}
