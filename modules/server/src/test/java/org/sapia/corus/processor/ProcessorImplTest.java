package org.sapia.corus.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.database.InMemoryArchiver;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.corus.interop.Status;
import org.sapia.corus.processor.task.PublishConfigurationChangeTask;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.ubik.util.Func;

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
  private HttpModule httpModule;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private InternalServiceContext serviceContext;
  
  @Mock
  private TaskManager tm;
  
  private ProcessorImpl   processor;
  
  private DbMap<String, ExecConfig> execConfigs;
  private DbMap<String, Process>    processes;
  private Properties corusServerProperties;
  
  @Before
  public void setUp() throws Exception {
    processor = new ProcessorImpl();
    processor.setDb(db);
    processor.setConfiguration(conf);
    processor.setDeployer(deployer);
    processor.setTaskManager(tm);
    processor.setEvents(events);
    processor.setHttpModule(httpModule);
    processor.setServerContext(serverContext);
    
    corusServerProperties = new Properties();
    execConfigs = new InMemoryDbMap<String, ExecConfig>(new ClassDescriptor<ExecConfig>(ExecConfig.class), new InMemoryArchiver<String, ExecConfig>(), new Func<ExecConfig, JsonInput>() {
      public ExecConfig call(JsonInput arg0) {
        return ExecConfig.fromJson(arg0);
      }
    });
    processes   = new InMemoryDbMap<String, Process>(new ClassDescriptor<Process>(Process.class), new InMemoryArchiver<String, Process>(),  new Func<Process, JsonInput>() {
      public Process call(JsonInput arg0) {
        return Process.fromJson(arg0);
      }
    });
    
    when(db.getDbMap(eq(String.class), eq(ExecConfig.class), anyString())).thenReturn(execConfigs);
    when(db.getDbMap(eq(String.class), eq(Process.class), anyString())).thenReturn(processes);
    
    when(serverContext.getServices()).thenReturn(serviceContext);
    when(serviceContext.getTaskManager()).thenReturn(tm);
//    when(tm.executeBackground(any(), any(), any());
    when(serverContext.getCorusProperties()).thenReturn(corusServerProperties);
    
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
  
  @Test
  public void testProcessConfigurationUpdate_defaultValueDisabled() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false");
    processor.start();
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isFalse();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_defaultValueActive() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_enabling() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false");
    processor.start();
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.ADD, CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true", PropertyScope.SERVER);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
}
  
  @Test
  public void testProcessConfigurationUpdate_disabling() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.ADD, CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false", PropertyScope.SERVER);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isFalse();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_deleteProcessProperty() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.REMOVE, CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false", PropertyScope.SERVER);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_active_otherServerProperty() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.REMOVE, "corus.server.whatever", "flagala", PropertyScope.SERVER);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_active_processPropertyAdded() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.ADD, "client.custom.param1", "val1", PropertyScope.PROCESS);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerExecutedConfigurationChangeTask(new Property[] { new Property("client.custom.param1", "val1") }, new Property[0]);
  }
  
  @Test
  public void testProcessConfigurationUpdate_active_processPropertyRemoved() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "true");
    processor.start();
    
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.REMOVE, "client.custom.param1", "val1", "cat3", PropertyScope.PROCESS);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isTrue();
    assertTaskManagerExecutedConfigurationChangeTask(new Property[0], new Property[] { new Property("client.custom.param1", "val1", "cat3") });
  }
  
  @Test
  public void testProcessConfigurationUpdate_inactive_processPropertyAdded() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false");
    processor.start();
    
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.ADD, "client.custom.param1", "val1", PropertyScope.PROCESS);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isFalse();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }
  
  @Test
  public void testProcessConfigurationUpdate_inactive_processPropertyRemoved() throws Exception {
    corusServerProperties.setProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false");
    processor.start();
    
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.REMOVE, "client.custom.param1", "val1", "cat3", PropertyScope.PROCESS);
    processor.doHandlePropertyChangeEvent(event);
    
    assertThat(processor.isPublishProcessConfigurationChangeEnabled()).isFalse();
    assertTaskManagerDidntExecuteConfigurationChangeTask();
  }

  @SuppressWarnings("unchecked")
  protected void assertTaskManagerDidntExecuteConfigurationChangeTask() {
    verify(tm, never()).execute(any(PublishConfigurationChangeTask.class), any(TaskParams.class));
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void assertTaskManagerExecutedConfigurationChangeTask(Property[] eAddedProperties, Property[] eRemovedProperties) {
    ArgumentCaptor<TaskParams> taskParamCaptor = ArgumentCaptor.forClass(TaskParams.class); 
    verify(tm).execute(any(PublishConfigurationChangeTask.class), taskParamCaptor.capture());
    
    TaskParams actualParams = taskParamCaptor.getValue();
    
    assertThat((List<Property>) actualParams.getParam1()).containsOnly(eAddedProperties);
    assertThat((List<Property>) actualParams.getParam2()).containsOnly(eRemovedProperties);
  }
  
}
