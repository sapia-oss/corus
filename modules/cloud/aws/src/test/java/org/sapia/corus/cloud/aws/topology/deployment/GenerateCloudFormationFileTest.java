package org.sapia.corus.cloud.aws.topology.deployment;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;

public class GenerateCloudFormationFileTest {

  private AwsTopologyDeploymentConf    conf;
  private AwsTopologyDeploymentContext context;
 
  @Mock
  private AmazonCloudFormation         cf;
    
  @Mock
  private AmazonEC2                    ec2;
  
  private Topology                     topology;

  private GenerateCloudFormationFile   generate;
  
  @Before
  public void setUp() throws Exception {
    topology = Topology.newInstance(new File("etc/basic_topology.xml"));
        
    conf = new AwsTopologyDeploymentConf();
    conf.withCorusCredentials(new CorusCredentials("test-app-id", "test-app-key"));
    conf.withTopology(topology);
    conf.withCloudFormationFileName("test-cloud-formation.json");
    conf.withCloudFormationOutputDir(new File("target"));
    conf.withEnvironment("dev");
    
    context  = new AwsTopologyDeploymentContext(conf, cf, ec2);
    
    generate = new GenerateCloudFormationFile();
  }
  
  @Test
  public void testExecute() throws Exception {
    generate.execute(context);
  }

}
