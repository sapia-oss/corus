package org.sapia.corus.processor;

import junit.framework.TestCase;

import org.sapia.corus.admin.services.deployer.dist.Dependency;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.Java;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.deployer.TestDeployer;
import org.sapia.corus.util.TestProgressQueue;

public class ProcessDependencyFilterTest extends TestCase {
  
  private TestDeployer deployer;
  private TestProcessor processor;
  private ProcessDependencyFilter filter;
  
  private Distribution maindist;
  private ProcessConfig mainproc;
  
  private Distribution maindist2;
  private ProcessConfig mainproc2;
  
  private Distribution dependantDist;
  private ProcessConfig dependantProc;
  
  private Distribution dependantDist2;
  private ProcessConfig dependantProc2;


  protected void setUp() throws Exception {
    super.setUp();
    
    deployer = new TestDeployer();
    processor = new TestProcessor();
    
    // main dist 1
    maindist = new Distribution();
    maindist.setName("maindist");
    maindist.setVersion("1.0");

    mainproc = new ProcessConfig();
    mainproc.setName("mainproc");
    Java starter = new Java();
    starter.setProfile("test");
    mainproc.addStarter(starter);
    maindist.addProcess(mainproc);
    
    // main dist 2
    maindist2 = new Distribution();
    maindist2.setName("maindist2");
    maindist2.setVersion("1.0");

    mainproc2 = new ProcessConfig();
    mainproc2.setName("mainproc2");
    starter = new Java();
    starter.setProfile("test");
    mainproc2.addStarter(starter);
    maindist2.addProcess(mainproc2);
    
    // dependent dist 1: depends on maindist 1.0
    dependantDist = new Distribution();
    dependantDist.setName("depdist");
    dependantDist.setVersion("1.0");
    
    dependantProc = new ProcessConfig();
    dependantProc.setName("dep-proc");
    Dependency dep = dependantProc.createDependency();
    dep.setDist("maindist");
    dep.setVersion("1.0");
    dep.setProcess("mainproc");
    dep.setProfile("test");
    starter = new Java();
    starter.setProfile("test");
    dependantProc.addStarter(starter);
    dependantDist.addProcess(dependantProc);
    
    // dependent dist 2: depends on maindist 1.0, maindist 2.0
    dependantDist2 = new Distribution();
    dependantDist2.setName("depdist2");
    dependantDist2.setVersion("1.0");
    
    dependantProc2 = new ProcessConfig();
    dependantProc2.setName("dep-proc2");
    dep = dependantProc.createDependency();
    dep.setDist("maindist");
    dep.setVersion("1.0");
    // not setting profile
    dep.setProcess("mainproc");

    dep = dependantProc.createDependency();
    dep.setDist("maindist2");
    dep.setVersion("1.0");
    dep.setProcess("mainproc2");
    dep.setProfile("test");
    starter = new Java();
    starter.setProfile("test");
    dependantProc2.addStarter(starter);
    dependantDist2.addProcess(dependantProc2);
    
    deployer.getDistributionDatabase().addDistribution(maindist);
    deployer.getDistributionDatabase().addDistribution(maindist2);
    deployer.getDistributionDatabase().addDistribution(dependantDist);
    deployer.getDistributionDatabase().addDistribution(dependantDist2);
    
    filter = new ProcessDependencyFilter(new TestProgressQueue());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testSingleDependency(){
    filter.addRootProcess(dependantDist, dependantProc, "test");
    filter.addRootProcess(dependantDist2, dependantProc2, "test");
    filter.filterDependencies(deployer, processor);
  
    /*
    for(ProcessRef p:filter.getFilteredProcesses()){
      System.out.println(p);
    }*/
    
    assertEquals(maindist2, filter.getFilteredProcesses().get(0).getDist());
    assertEquals(maindist, filter.getFilteredProcesses().get(1).getDist());
    assertEquals(dependantDist2, filter.getFilteredProcesses().get(2).getDist());
    assertEquals(dependantDist, filter.getFilteredProcesses().get(3).getDist());
   
  }
  

}
