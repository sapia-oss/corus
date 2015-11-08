package org.sapia.corus.processor.task;

import org.junit.Before;
import org.mockito.Mockito;
import org.sapia.console.CmdLine;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Java;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.ProcessorConfigurationImpl;
import org.sapia.corus.taskmanager.core.TaskManager;

/**
 * @author Yanick Duchesne
 */
public class TestBaseTask {
  
  protected TestServerContext ctx;
  protected ProcessRepository db;
  protected TaskManager tm;
  protected Processor processor;
  protected ProcessorConfigurationImpl processorConf;
  
  @Before
  protected void setUp() throws Exception {
    ctx           = TestServerContext.create();
    db            = ctx.getServices().getProcesses();
    tm            = ctx.lookup(TaskManager.class);
    processor     = ctx.lookup(Processor.class);
    processorConf = (ProcessorConfigurationImpl)processor.getConfiguration();
  }
  
  protected Distribution createDistribution(String name, String version) throws DuplicateDistributionException{
    Distribution dist = new Distribution();
    dist.setName(name);
    dist.setVersion(version);
    dist.setBaseDir("basedir");
    ctx.getDepl().getDistributionDatabase().addDistribution(dist);
    return dist;
  }
  
  protected ProcessConfig createProcessConfig(Distribution dist, String processName, String profile){
    ProcessConfig conf = new ProcessConfig(){
      public OptionalValue<StarterResult> toCmdLine(Env env) throws MissingDataException {
        return OptionalValue.of(new StarterResult(StarterType.JAVA, new CmdLine(), true));
      }
    };
    conf.setName(processName);
    Java javaStarter = new Java();
    javaStarter.setProfile(profile);
    javaStarter.setCorusHome(System.getProperty("user.dir"));
    javaStarter.setMainClass("none");
    conf.addStarter(javaStarter);
    
    dist.addProcess(conf);
    return conf;
  }
  
  protected Process createProcess(Distribution dist, ProcessConfig conf, String profile){
    DistributionInfo info = new DistributionInfo(dist.getName(), dist.getVersion(), profile, conf.getName());
    Process proc = new Process(info);
    proc.setProcessDir("processdir");
    proc.setOsPid(Integer.toString(Integer.MAX_VALUE));
    proc = Mockito.spy(proc);
    ctx.getProc().getProcessDB().addProcess(proc);
    return proc;
  }
  
}
