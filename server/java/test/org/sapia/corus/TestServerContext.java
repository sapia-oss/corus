package org.sapia.corus;

import org.sapia.corus.admin.services.configurator.Configurator;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.port.PortManager;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.configurator.TestConfigurator;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.deployer.TestDeployer;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.corus.event.TestDispatcher;
import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.TestProcessor;
import org.sapia.corus.processor.task.ProcessorTaskStrategy;
import org.sapia.corus.processor.task.TestProcessorTaskStrategy;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TestTaskManager;
import org.sapia.ubik.net.TCPAddress;

public class TestServerContext extends ServerContext{
  
  public TestServerContext() {
    super(
        new TCPAddress("localhost", 33000), 
        "test", 
        "home",
        new InternalServiceContext());
  }
  
  public static TestServerContext create(){
    TestDispatcher    disp  = new TestDispatcher();
    TestDeployer      depl  = new TestDeployer();
    TestProcessor     proc  = new TestProcessor();
    TestPortManager   ports = new TestPortManager();
    TestConfigurator  tc    = new TestConfigurator();
    TestServerContext ctx   = new TestServerContext();
    TestTaskManager tm    = new TestTaskManager(ctx);
    TestProcessorTaskStrategy procStrat = new TestProcessorTaskStrategy();
    
    ctx.getServices().bind(EventDispatcher.class, disp);
    ctx.getServices().bind(Deployer.class, depl);
    ctx.getServices().bind(Processor.class, proc);
    ctx.getServices().bind(ProcessorTaskStrategy.class, procStrat);
    ctx.getServices().bind(PortManager.class, ports);
    ctx.getServices().bind(TaskManager.class, tm);
    ctx.getServices().bind(Configurator.class, tc);
    ctx.getServices().bind(ProcessRepository.class, proc.getProcessRepository());
    ctx.getServices().bind(DistributionDatabase.class, depl.getDistributionDatabase());
    
    return ctx;
  }
}
