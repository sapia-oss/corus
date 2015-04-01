package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.KillPreferences;
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
  public ProgressQueue execConfig(ExecConfigCriteria crit) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  @Override
  public void setExecConfigEnabled(ExecConfigCriteria crit, boolean enabled) {
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
  public void kill(ProcessCriteria criteria, KillPreferences prefs) {
  }

  @Override
  public void kill(String corusPid, KillPreferences prefs) throws ProcessNotFoundException{
  }
  
  @Override
  public void confirmShutdown(String corusPid) throws ProcessNotFoundException {
  }
  
  @Override
  public void restart(String pid, KillPreferences prefs) throws ProcessNotFoundException{
  }
  
  @Override
  public void restartByAdmin(String pid, KillPreferences prefs) throws ProcessNotFoundException{
  }
  
  @Override
  public void restart(ProcessCriteria criteria, KillPreferences prefs) {
  }
  
  @Override
  public ProgressQueue resume(ProcessCriteria criteria) {
    return new ProgressQueueImpl();
  }
  
  public ProgressQueue resume() {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  @Override
  public void addExecConfig(ExecConfig conf) {
  }
  
  @Override
  public List<ExecConfig> getExecConfigs(ExecConfigCriteria crit) {
    return new ArrayList<ExecConfig>();
  }
  
  @Override
  public void removeExecConfig(ExecConfigCriteria crit) {
  }
  
  @Override
  public String getRoleName() {
    return Processor.ROLE;
  }

  public ProcessRepository getProcessDB(){
    return db;
  }
  
  @Override
  public void archiveExecConfigs(RevId revId) {
    
  }
  
  @Override
  public void unarchiveExecConfigs(RevId revId) {
  }
  
  @Override
  public void clean() {
  }
  
  @Override
  public void dump(JsonStream stream) {
  }
  
  @Override
  public void load(JsonInput dump) {
  }
}
