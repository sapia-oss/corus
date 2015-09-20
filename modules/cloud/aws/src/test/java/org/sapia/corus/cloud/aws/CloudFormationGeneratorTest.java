package org.sapia.corus.cloud.aws;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.cloud.topology.Topology;

public class CloudFormationGeneratorTest {

  private CloudFormationGenerator gen;
  
  @Before
  public void setUp() throws Exception {
    gen = new CloudFormationGenerator.Builder().build();
  }
  
  @Test
  public void testGenerateAsString() throws Exception {
    gen.generateString(Topology.newInstance(new File("etc/basic_topology.xml")), "dev", "/aws/cloud_formation.ftl");
  }

}
