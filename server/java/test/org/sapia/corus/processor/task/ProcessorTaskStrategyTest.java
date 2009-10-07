package org.sapia.corus.processor.task;

import junit.framework.TestCase;

import org.sapia.corus.TestServerContext;
import org.sapia.corus.admin.services.processor.DistributionInfo;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.admin.services.processor.ProcessorConfigurationImpl;
import org.sapia.corus.admin.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TestTaskLog;
import org.sapia.corus.taskmanager.core.TestTaskManager;
import org.sapia.corus.util.IntProperty;

public class ProcessorTaskStrategyTest extends TestCase{

  protected DistributionInfo dist;
  protected TestServerContext ctx;
  protected TestTaskManager tm;
  protected TaskExecutionContext taskContext;
  protected TestProcessorTaskStrategy strategy;
  protected ProcessRepository processes;
  protected ProcessorConfigurationImpl processorConf;
  protected Processor processor;
  
  public ProcessorTaskStrategyTest(String name) {
    super(name);
  }
  
  @Override
  protected void setUp() throws Exception {
    dist = new DistributionInfo("test", "1.0", "test", "testVm");
    ctx = TestServerContext.create();
    processes = ctx.getServices().getProcesses();
    tm = (TestTaskManager)ctx.lookup(TaskManager.class);
    strategy = (TestProcessorTaskStrategy)ctx.lookup(ProcessorTaskStrategy.class);
    processor = ctx.lookup(Processor.class);
    processorConf = (ProcessorConfigurationImpl)processor.getConfiguration();
    taskContext = new TaskExecutionContext(
        new Task(){
          @Override
          public Object execute(TaskExecutionContext ctx) throws Throwable {
            return null;
          }
        },
        new TestTaskLog(),
        ctx,
        tm
        );
  }
  
  public void testAttemptKill() throws Throwable{
    Process          proc = new Process(dist);
    proc.setMaxKillRetry(3);
    
    super.assertTrue(
       !strategy.attemptKill(
          taskContext, 
          ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN, 
          proc, 1));
    proc.confirmKilled();
    super.assertTrue(
        strategy.attemptKill(
            taskContext, 
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN, 
            proc, 1));    
  }
  
  public void testCleanupProcess() throws Throwable{
    Process          proc = new Process(dist);
    ctx.getServices().getProcesses().getActiveProcesses().addProcess(proc);
    strategy.cleanupProcess(taskContext, proc);
    super.assertTrue(!ctx.getServices().getProcesses().getActiveProcesses().containsProcess(proc.getProcessID()));    
  }
  
  public void testForcefullKillNoPid() throws Throwable{
    Process          proc = new Process(dist);
    processes.getActiveProcesses().addProcess(proc);
    boolean killed = strategy.forcefulKill(taskContext, 
        ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
        proc.getProcessID());
    super.assertTrue(!killed);
    super.assertTrue(strategy.noPid);
  }
  
  public void testForcefullKillProcessRestart() throws Throwable{
    Process          proc = new Process(dist);
    proc.setOsPid("1234");
    processes.getActiveProcesses().addProcess(proc);
    processorConf.setRestartInterval(new IntProperty(1));
    Thread.sleep(1200);
    boolean killed = strategy.forcefulKill(taskContext, 
        ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
        proc.getProcessID());
    
    super.assertTrue(killed);
    super.assertTrue(strategy.nativeKill);    
    super.assertTrue(strategy.restart);
  }
  
  public void testForcefullKillNoRestart() throws Throwable{
    Process          proc = new Process(dist);
    proc.setOsPid("1234");
    processes.getActiveProcesses().addProcess(proc);
    Thread.sleep(200);
    boolean killed = strategy.forcefulKill(taskContext, 
        ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
        proc.getProcessID());

    super.assertTrue(killed);
    super.assertTrue(strategy.nativeKill);    
    super.assertTrue(!strategy.restart);

  }
  
  public void testForcefullKillRestartInvalid() throws Throwable{
    Process          proc = new Process(dist);
    proc.setOsPid("1234");
    processes.getActiveProcesses().addProcess(proc);
    Thread.sleep(200);
    boolean killed = strategy.forcefulKill(taskContext, 
        ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 
        proc.getProcessID());
    
    super.assertTrue(killed);
    super.assertTrue(strategy.nativeKill);    
    super.assertTrue(strategy.restartInvalid);
  }
}
