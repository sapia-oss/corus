package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.interop.Status;

public class TestProcessor implements Processor{
  
  private TestProcessRepository      db            = new TestProcessRepository();
  private ProcessorConfigurationImpl configuration = new  ProcessorConfigurationImpl();
  
  public TestProcessRepository getProcessRepository(){
    return db;
  }

  @Override
  public ProcessorConfiguration getConfiguration() {
    return configuration;
  }
  
  public ProcessorConfigurationImpl getConfigurationImpl(){
    return configuration;
  }
  
  @Override
  public ProgressQueue exec(String execConfigName) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }

  @Override  
  public ProgressQueue exec(ProcessCriteria criteria, int instances) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }

  @Override  
  public Process getProcess(String corusPid) throws ProcessNotFoundException {
    return db.getProcess(corusPid);
  }
  
  @Override
  public List<Process> getProcesses(ProcessCriteria criteria) {
    return db.getProcesses(criteria);
  }
  
  @Override
  public List<Process> getProcessesWithPorts() {
    return new ArrayList<Process>();
  }
  
  @Override
  public List<Status> getStatus(ProcessCriteria criteria) {
    return new ArrayList<Status>();
  }

  @Override
  public ProcStatus getStatusFor(String corusPid) throws ProcessNotFoundException {
    return new ProcStatus(getProcess(corusPid));
  }
  
  @Override
  public void kill(ProcessCriteria criteria, boolean suspend) {
  }

  @Override
  public void kill(String corusPid, boolean suspend) throws ProcessNotFoundException{
  }
  
  @Override
  public void restart(String pid) throws ProcessNotFoundException{
  }
  
  @Override
  public void restartByAdmin(String pid) throws ProcessNotFoundException{
  }
  
  @Override
  public void restart(ProcessCriteria criteria) {
  }
  
  public ProgressQueue resume() {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  @Override
  public void addExecConfig(ExecConfig conf) {
  }
  
  @Override
  public List<ExecConfig> getExecConfigs() {
    return new ArrayList<ExecConfig>();
  }
  
  @Override
  public void removeExecConfig(Arg name) {
  }
  
  @Override
  public String getRoleName() {
    return Processor.ROLE;
  }

  public ProcessRepository getProcessDB(){
    return db;
  }
}
