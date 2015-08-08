package org.sapia.corus.client.services.deployer;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.ChecksumPreference.ChecksumAlgo;

public class ChecksumPreferenceTest {
  

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCreation() {
    assertEquals(ChecksumAlgo.MD5, ChecksumPreference.forMd5().getAlgo());
  }

  @Test
  public void testAssignClientChecksum() {
    ChecksumPreference pref = ChecksumPreference.forMd5();
    
    pref.assignClientChecksum("test");
    
    assertEquals("test", pref.getClientChecksum().get());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetClientChecksum_not_set() {
    ChecksumPreference pref = ChecksumPreference.forMd5();

    pref.getClientChecksum().get();
  }
  
  @Test
  public void testSerialization() {
    ChecksumPreference pref = ChecksumPreference.forMd5();
    pref.assignClientChecksum("test");
    byte[] data = SerializationUtils.serialize(pref);
    
    ChecksumPreference copy = (ChecksumPreference)SerializationUtils.deserialize(data);
    assertEquals(pref, copy);
  }
  
  @Test
  public void testEquals() {
    ChecksumPreference c1 = ChecksumPreference.forMd5().assignClientChecksum("c1");
    ChecksumPreference c2 = ChecksumPreference.forMd5().assignClientChecksum("c1");
  
    assertEquals(c1, c2);
  }

  @Test
  public void testEquals_false() {
    ChecksumPreference c1 = ChecksumPreference.forMd5().assignClientChecksum("c1");
    ChecksumPreference c2 = ChecksumPreference.forMd5().assignClientChecksum("c2");
  
    assertNotEquals(c1, c2);
  }
  
  @Test
  public void testEquals_one_checksum_not_set() {
    ChecksumPreference c1 = ChecksumPreference.forMd5().assignClientChecksum("c1");
    ChecksumPreference c2 = ChecksumPreference.forMd5();
  
    assertNotEquals(c1, c2);
  }
  
  @Test
  public void testEquals_both_checksums_not_set() {
    ChecksumPreference c1 = ChecksumPreference.forMd5();
    ChecksumPreference c2 = ChecksumPreference.forMd5();
  
    assertEquals(c1, c2);
  }
}
