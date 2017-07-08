package org.sapia.corus.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

public class CorusUserDataParserTest {

  @Test
  public void testParseEmpty() throws Exception {
    CorusUserData ud = CorusUserDataParser.parse(getResource("emptyUserData.json.txt"));
    assertEquals(0, ud.getServerProperties().size());
    assertEquals(0, ud.getServerTags().size());
    assertEquals(0, ud.getProcessProperties().size());
  }
  
  @Test
  public void testParseServerPropertiesOnly() throws Exception {
    CorusUserData ud = CorusUserDataParser.parse(getResource("serverPropertiesUserData.json.txt"));
    assertEquals(0, ud.getServerTags().size());
    assertEquals(0, ud.getProcessProperties().size());
    
    assertEquals("theValue1", ud.getServerProperties().getProperty("theName1"));
    assertEquals("theValue2", ud.getServerProperties().getProperty("theName2"));
  }  
  
  @Test
  public void testParseServerTagsOnly() throws Exception {
    CorusUserData ud = CorusUserDataParser.parse(getResource("serverTagsUserData.json.txt"));
    assertEquals(0, ud.getServerProperties().size());
    assertEquals(0, ud.getProcessProperties().size());
    
    assertTrue(ud.getServerTags().contains("tag1"));
    assertTrue(ud.getServerTags().contains("tag2"));
  }
  
  @Test
  public void testParseProcessesPropertiesOnly() throws Exception {
    CorusUserData ud = CorusUserDataParser.parse(getResource("processesPropertiesUserData.json.txt"));
    assertEquals(0, ud.getServerTags().size());
    assertEquals(0, ud.getServerProperties().size());
    
    assertEquals("theValue1", ud.getProcessProperties().getProperty("theName1"));
    assertEquals("theValue2", ud.getProcessProperties().getProperty("theName2"));
  }
  
  
  @Test
  public void testParseFull() throws Exception {
    CorusUserData ud = CorusUserDataParser.parse(getResource("fullUserData.json.txt"));
    
    assertEquals("test-domain", ud.getDomain().get());
    assertEquals(RepoRole.SERVER, ud.getRepoRole().get());
    
    assertEquals("theValue1", ud.getServerProperties().getProperty("theName1"));
    assertEquals("theValue2", ud.getServerProperties().getProperty("theName2"));
    
    assertTrue(ud.getServerTags().contains("tag1"));
    assertTrue(ud.getServerTags().contains("tag2"));    
    
    assertEquals("theValue1", ud.getProcessProperties().getProperty("theName1"));
    assertEquals("theValue2", ud.getProcessProperties().getProperty("theName2"));
  }      
  
  private InputStream getResource(String name) throws IOException  {
    InputStream is = getClass().getResourceAsStream(name);
    if (is == null) {
      throw new IOException("Resource not found: " + name);
    }
    return is;
  }
}
