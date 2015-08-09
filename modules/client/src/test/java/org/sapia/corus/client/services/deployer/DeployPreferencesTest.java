package org.sapia.corus.client.services.deployer;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

public class DeployPreferencesTest {

  DeployPreferences prefs;
  
  @Before
  public void setUp() throws Exception {
    prefs = DeployPreferences.newInstance();
  }

  @Test
  public void testExecuteDeployScripts() {
    prefs.executeDeployScripts();
    assertTrue(prefs.isExecuteDeployScripts());
  }

  @Test
  public void testSetExecDeployScripts() {
    prefs.setExecDeployScripts(true);
    assertTrue(prefs.isExecuteDeployScripts());
  }

  @Test
  public void testSetChecksum() {
    prefs.setChecksum(ChecksumPreference.forMd5());
    assertEquals(ChecksumPreference.forMd5(), prefs.getChecksum().get());
  }

  @Test
  public void testSerialization() {
    prefs.setChecksum(ChecksumPreference.forMd5());
    prefs.setExecDeployScripts(true);
    
    byte[] payload = SerializationUtils.serialize(prefs);
    
    DeployPreferences copy = (DeployPreferences) SerializationUtils.deserialize(payload);
    
    assertEquals(prefs, copy);
    
  }

}
