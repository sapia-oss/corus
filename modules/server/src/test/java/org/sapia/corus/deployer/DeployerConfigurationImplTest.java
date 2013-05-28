package org.sapia.corus.deployer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DeployerConfigurationImplTest {
  
  private DeployerConfigurationImpl original, other;
  
  @Before
  public void setUp() {
    original = new DeployerConfigurationImpl();
    original.setDeployDir("deployDir");
    original.setFileLockTimeout(10000);
    original.setRepoDir("repoDir");
    original.setScriptDir("scriptDir");
    original.setTempDir("tempDir");
    original.setUploadDir("uploadDir");
    other = new DeployerConfigurationImpl();
  }

  @Test
  public void testCopyFrom() {
    other.copyFrom(original);
    assertEquals(other.getDeployDir(), original.getDeployDir());
    assertEquals(other.getFileLockTimeout(), original.getFileLockTimeout());
    assertEquals(other.getRepoDir(), original.getRepoDir());
    assertEquals(other.getScriptDir(), original.getScriptDir());
    assertEquals(other.getTempDir(), original.getTempDir());    
    assertEquals(other.getUploadDir(), original.getUploadDir());
  }

}
