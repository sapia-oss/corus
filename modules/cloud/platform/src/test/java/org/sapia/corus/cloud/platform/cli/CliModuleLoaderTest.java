package org.sapia.corus.cloud.platform.cli;

import org.junit.Before;
import org.junit.Test;

public class CliModuleLoaderTest {

  private CliModuleLoader loader;
  
  @Before
  public void setUp() throws Exception {
    loader = new CliModuleLoader();
  }

  @Test
  public void testLoad() {
    loader.load("test-provider", "test-command");
  }
  
  @Test(expected = IllegalStateException.class)
  public void testLoad_invalid_command() {
    loader.load("test-provider", "invalid-command");
  }

  @Test(expected = IllegalStateException.class)
  public void testLoad_invalid_provider() {
    loader.load("test-invalid-provider", "test-command");
  }
}
