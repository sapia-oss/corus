package org.sapia.corus.admin;

import java.io.File;

import junit.framework.TestCase;

public class CorusFacadeImplTest extends TestCase {

  public CorusFacadeImplTest(String name){
    super(name);
  }
  
  public void testSplitValidDir(){
    Object[] info = CorusFacadeImpl.split("etc/*.xml");
    File baseDir = (File)info[0];
    assertEquals(System.getProperty("user.dir")+File.separator+"etc", baseDir.getAbsolutePath());
    assertEquals("*.xml", info[1]);
  }
  
  public void testSplitInvalidDir(){
    Object[] info = CorusFacadeImpl.split("foo/*.xml");
    File baseDir = (File)info[0];
    assertEquals(System.getProperty("user.dir"), baseDir.getAbsolutePath());
    assertEquals("*.xml", info[1]);
  }  
  
  
  public void testSplitPatternNotAtStart(){
    Object[] info = CorusFacadeImpl.split("etc/demo*.xml");
    File baseDir = (File)info[0];
    assertEquals(System.getProperty("user.dir")+File.separator+"etc", baseDir.getAbsolutePath());
    assertEquals("demo*.xml", info[1]);
  }  
}
