package org.sapia.corus.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.corus.interop.Status;
import org.sapia.corus.taskmanager.core.TaskManager;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorImplTest {
  
  @Mock
  private DbModule db;
  
  @Mock 
  private ProcessorConfiguration conf;
  
  @Mock
  private Deployer deployer;
  
  @Mock
  private EventDispatcher events;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private InternalServiceContext serviceContext;
  
  @Mock
  private TaskManager tm;
  
  private ProcessorImpl   processor;
  
  private DbMap<String, ExecConfig> execConfigs;
  private DbMap<String, Process>    processes;
  
  @Before
  public void setUp() throws Exception {
    processor = new ProcessorImpl();
    processor.setDb(db);
    processor.setConfiguration(conf);
    processor.setDeployer(deployer);
    processor.setEvents(events);
    processor.setServerContext(serverContext);
    
    execConfigs = new InMemoryDbMap<String, ExecConfig>(new ClassDescriptor<ExecConfig>(ExecConfig.class));
    processes   = new InMemoryDbMap<String, Process>(new ClassDescriptor<Process>(Process.class));
    
    when(db.getDbMap(eq(String.class), eq(ExecConfig.class), anyString())).thenReturn(execConfigs);
    when(db.getDbMap(eq(String.class), eq(Process.class), anyString())).thenReturn(processes);
    
    when(serverContext.getServices()).thenReturn(serviceContext);
    when(serviceContext.getTaskManager()).thenReturn(tm);
    
    processor.init();
  }

  @Test
  public void testSetExecConfigEnabled_true() {
    execConfigs.put("test", conf("test"));
    processor.setExecConfigEnabled(ExecConfigCriteria.builder().name("test").build(), true);
    assertTrue(processor.getExecConfigs(ExecConfigCriteria.builder().all().build()).get(0).isEnabled());
  }
  
  @Test
  public void testSetExecConfigEnabled_false() {
    execConfigs.put("test", conf("test"));
    processor.setExecConfigEnabled(ExecConfigCriteria.builder().name("test").build(), false);
    assertFalse(processor.getExecConfigs(ExecConfigCriteria.builder().all().build()).get(0).isEnabled());
  }

  @Test
  public void testAddExecConfig() {
    processor.addExecConfig(conf("test"));
    assertEquals(1, processor.getExecConfigs(ExecConfigCriteria.builder().all().build()).size());
  }

  @Test
  public void testRemoveExecConfig() {
    processor.addExecConfig(conf("test"));
    processor.removeExecConfig(ExecConfigCriteria.builder().all().build());
    assertEquals(0, processor.getExecConfigs(ExecConfigCriteria.builder().all().build()).size());
  }

  @Test
  public void testGetProcess() throws Exception {
    Process proc = proc("1");
    processes.put(proc.getProcessID(), proc);
    assertEquals(proc, processor.getProcess(proc.getProcessID()));
  }

  @Test
  public void testGetProcesses() {
    Process proc1 = proc("1");
    Process proc2 = proc("2");
    
    processes.put(proc1.getProcessID(), proc1);
    processes.put(proc2.getProcessID(), proc2);
    
    List<Process> results = processor.getProcesses(ProcessCriteria.builder()
        .distribution(ArgMatchers.parse(proc1.getDistributionInfo().getName())).build()); 
    assertEquals(1, results.size());
    assertEquals(proc1, results.get(0));
  }

  @Test
  public void testGetProcessesWithPorts() {
    Process proc1 = proc("1");
    Process proc2 = proc("2");
  
    proc1.addActivePort(new ActivePort("test", 1));
    
    processes.put(proc1.getProcessID(), proc1);
    processes.put(proc2.getProcessID(), proc2);
    
    List<Process> results = processor.getProcessesWithPorts();
    assertEquals(1, results.size());
    assertEquals(proc1, results.get(0));
  }

  @Test
  public void testGetStatus() throws Exception {
    Process proc = proc("1");
    processes.put(proc.getProcessID(), proc);
    proc.status(new Status());
    
    processor.getStatus(ProcessCriteria.builder().all());
  }

  @Test
  public void testGetStatusFor() throws Exception {
    Process proc = proc("1");
    processes.put(proc.getProcessID(), proc);
    proc.status(new Status());
    
    ProcStatus status = processor.getStatusFor(proc.getProcessID());
    assertNotNull(status);
  }

  @Test
  public void testArchiveExecConfigs() {
    ExecConfig conf = conf("test");
    processor.addExecConfig(conf);
    
    processor.archiveExecConfigs(RevId.valueOf("123"));
    processor.removeExecConfig(ExecConfigCriteria.builder().all().build());
    processor.unarchiveExecConfigs(RevId.valueOf("123"));
    
    assertEquals(1, processor.getExecConfigs(ExecConfigCriteria.builder().all().build()).size());
  }

  private ExecConfig conf(String name) {
    ExecConfig cfg = new ExecConfig();
    cfg.setName(name);
    return cfg;
  }
  
  private Process proc(String distSuffix) {
    Process proc = new Process(new DistributionInfo("test-" + distSuffix, "v1", "prof", "proc"));
    return proc;
  }
  

}
