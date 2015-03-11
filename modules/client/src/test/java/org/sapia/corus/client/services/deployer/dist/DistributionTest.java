package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;

public class DistributionTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testNewInstanceInputStream() throws Exception {
    File f = new File("etc/test/testCorus.xml");
    InputStream is = new java.io.FileInputStream(f);
    try {
      Distribution.newInstance(is);
    } finally {
      is.close();
    }
  }

  @Test
  public void testSort() {
    Distribution dist1 = new Distribution("dist", "2.0");
    Distribution dist2 = new Distribution("a-dist", "1.0");
    Distribution dist3 = new Distribution("dist", "1.0");

    List<Distribution> dists = new java.util.ArrayList<Distribution>();

    dists.add(dist1);
    dists.add(dist2);
    dists.add(dist3);

    Collections.sort(dists);

    Assert.assertEquals(dist2, dists.get(0));
    Assert.assertEquals(dist3, dists.get(1));
    Assert.assertEquals(dist1, dists.get(2));

  }

  @Test
  public void testMatchesAny() {
    Distribution dist1 = new Distribution("dist", "2.0");
    ArgMatcher name = ArgMatchers.any();
    ArgMatcher version = ArgMatchers.any();
    Assert.assertTrue(dist1.matches(name, version));
  }

  @Test
  public void testMatchesAnyName() {
    Distribution dist1 = new Distribution("dist", "2.0");
    ArgMatcher name = ArgMatchers.any();
    ArgMatcher version = ArgMatchers.exact("2.0");
    Assert.assertTrue(dist1.matches(name, version));
  }

  @Test
  public void testMatchesAnyVersion() {
    Distribution dist1 = new Distribution("dist", "2.0");
    ArgMatcher name = ArgMatchers.exact("dist");
    ArgMatcher version = ArgMatchers.any();
    Assert.assertTrue(dist1.matches(name, version));
  }

  @Test
  public void testGetProcessConfig() {
    Distribution dist = new Distribution("dist", "2.0");
    for (int i = 0; i < 5; i++) {
      ProcessConfig pc = new ProcessConfig();
      pc.setName("proc_" + i);
      dist.addProcess(pc);
    }
    ProcessConfig proc = dist.getProcess("proc_2");
    Assert.assertEquals("proc_2", proc.getName());
  }

  @Test
  public void testGetProcessConfigs() {
    Distribution dist = new Distribution("dist", "2.0");
    dist.addProcess(new ProcessConfig("proc1"));
    dist.addProcess(new ProcessConfig("proc21"));
    dist.addProcess(new ProcessConfig("proc22"));

    List<ProcessConfig> match = dist.getProcesses(ArgMatchers.parse("proc2*"));
    Assert.assertEquals(2, match.size());
  }

  @Test
  public void testContainsProcess() {
    Distribution dist = new Distribution("dist", "2.0");
    dist.addProcess(new ProcessConfig("proc"));
    Assert.assertTrue(dist.containsProcess("proc"));
  }

  @Test
  public void testGetDistributionFileName() {
    Distribution dist = new Distribution("dist", "2.0");
    Assert.assertEquals("dist-2.0.zip", dist.getDistributionFileName());
  }

  @Test
  public void testTags() {
    Distribution dist = new Distribution("dist", "2.0");
    dist.setTags("tag1, tag2, tag3");
    Assert.assertTrue(dist.getTagSet().contains("tag1"));
    Assert.assertTrue(dist.getTagSet().contains("tag2"));
    Assert.assertTrue(dist.getTagSet().contains("tag3"));
  }
  
  
  @Test
  public void testMatchesDist() {
    Distribution dist = new Distribution("dist", "1.0");
    Pattern pattern = Matcheable.DefaultPattern.parse("dis*");
    assertTrue(dist.matches(pattern));
  }

  @Test
  public void testMatchesVersion() {
    Distribution dist = new Distribution("dist", "1.0");
    Pattern pattern = Matcheable.DefaultPattern.parse("1.*");
    assertTrue(dist.matches(pattern));
  }

}
