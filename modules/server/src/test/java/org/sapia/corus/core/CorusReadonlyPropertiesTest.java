package org.sapia.corus.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.FilePath;

public class CorusReadonlyPropertiesTest {

  private File outputDir;
  
  @Before
  public void setUp() {
    outputDir = FilePath.newInstance().addDir(System.getProperty("java.io.tmpdir")).addDir("corus-test").createFile();
    outputDir.mkdirs();
    
    if (!outputDir.exists()) {
      throw new IllegalStateException("Could not make directory: " + outputDir.getAbsolutePath());
    }
  }
  
  @After
  public void tearDown() {
    File[] toDelete = outputDir.listFiles();
    if (toDelete != null) {
      for (File d : toDelete) {
        d.delete();
      }
    }
    outputDir.delete();
  }
  
  @Test
  public void testSave_and_load() {
    Properties toSave = new Properties();
    toSave.setProperty("k", "v");
    CorusReadonlyProperties.save(toSave, outputDir, 33000, true);
    System.out.println("Saved to: " + outputDir.getAbsolutePath());
    
    Properties loaded = CorusReadonlyProperties.load(outputDir, 33000);
    assertEquals("v", loaded.getProperty("k"));
  }

  @Test
  public void testLoadInto() {
    Properties toSave = new Properties();
    toSave.setProperty("k", "v");
    CorusReadonlyProperties.save(toSave, outputDir, 33000, true);
    
    Properties loaded = new Properties();
    CorusReadonlyProperties.loadInto(loaded, outputDir, 33000);
    assertEquals("v", loaded.getProperty("k"));
  }

  @Test
  public void testSave_update() {
    Properties toSave = new Properties();
    toSave.setProperty("k1", "v1");
    CorusReadonlyProperties.save(toSave, outputDir, 33000, true);
    
    Properties update = new Properties();
    update.setProperty("k2", "v2");
    CorusReadonlyProperties.save(update, outputDir, 33000, false);

    Properties loaded = CorusReadonlyProperties.load(outputDir, 33000);
    assertEquals("v1", loaded.getProperty("k1"));
    assertEquals("v2", loaded.getProperty("k2"));
  }

}
