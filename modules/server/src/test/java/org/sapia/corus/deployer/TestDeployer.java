package org.sapia.corus.deployer;


public class TestDeployer extends BaseDeployer{
  
  public TestDeployer() {
    super.configuration.setDeployDir("deployDir");
    super.configuration.setTempDir("tmpDir");
    super.configuration.setRepoDir("repoDir");
  }
  
  public DistributionDatabase getDistributionDatabase(){
    return super.getDistributionDatabase();
  }

}
