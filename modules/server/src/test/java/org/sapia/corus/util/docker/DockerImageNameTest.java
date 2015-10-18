package org.sapia.corus.util.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DockerImageNameTest {
  
  @Test
  public void testWithUser() {
    DockerImageName img = new DockerImageName("test");
    assertEquals("usr", img.withUser("usr").getUser().get());
  }

  @Test
  public void testWithTag() {
    DockerImageName img = new DockerImageName("test");
    assertEquals("1.0", img.withTag("1.0").getTag().get());
  }

  @Test
  public void testWithImage() {
    DockerImageName img = new DockerImageName("test");
    assertEquals("test2", img.withImage("test2").getImage());
  }
  
  @Test
  public void testParse_with_user() {
    DockerImageName img = DockerImageName.parse("usr/test");
    assertEquals("usr", img.getUser().get());
    assertEquals("test", img.getImage());
  }
  
  @Test
  public void testParse_with_user_tag() {
    DockerImageName img = DockerImageName.parse("usr/test:1.0");
    assertEquals("usr", img.getUser().get());
    assertEquals("test", img.getImage());
    assertEquals("1.0", img.getTag().get());
  }
  
  @Test
  public void testParse_with_tag() {
    DockerImageName img = DockerImageName.parse("test:1.0");
    assertTrue(img.getUser().isNull());
    assertEquals("test", img.getImage());
    assertEquals("1.0", img.getTag().get());
  }
  
  @Test
  public void testParse_with_name_only() {
    DockerImageName img = DockerImageName.parse("test");
    assertTrue(img.getUser().isNull());
    assertEquals("test", img.getImage());
    assertTrue(img.getTag().isNull());
  }
  
  @Test
  public void testToString_with_user() {
    DockerImageName img = new DockerImageName("test").withUser("usr");
    assertEquals("usr/test", img.toString());
  }
  
  @Test
  public void testToString_with_user_tag() {
    DockerImageName img = new DockerImageName("test").withUser("usr").withTag("1.0");
    assertEquals("usr/test:1.0", img.toString());
  }
  
  @Test
  public void testToString_with_tag() {
    DockerImageName img = new DockerImageName("test").withTag("1.0");
    assertEquals("test:1.0", img.toString());
  }
  
  @Test
  public void testToString_with_name_only() {
    DockerImageName img = DockerImageName.parse("test");
    assertTrue(img.getUser().isNull());
    assertEquals("test", img.getImage());
    assertTrue(img.getTag().isNull());
  }
}
