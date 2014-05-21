package org.sapia.corus.client.sort;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.corus.client.services.processor.ExecConfig;

public class ExecConfigComparatorsTest {
  
  @Test
  public void testForName() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c2", "p1");
    
    assertTrue(ExecConfigComparators.forName().compare(c1, c2) < 0);
    assertTrue(ExecConfigComparators.forName().compare(c2, c1) > 0);
    assertTrue(ExecConfigComparators.forName().compare(c1, c1) == 0);
  }

  @Test
  public void testForProfile() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c1", "p2");
    
    assertTrue(ExecConfigComparators.forProfile().compare(c1, c2) < 0);
    assertTrue(ExecConfigComparators.forProfile().compare(c2, c1) > 0);
    assertTrue(ExecConfigComparators.forProfile().compare(c1, c1) == 0);
  }
  
  private ExecConfig config(String name, String profile) {
    ExecConfig c = new ExecConfig();
    c.setName(name);
    c.setProfile(profile);
    return c;
  }

}
