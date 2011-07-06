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
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.interop.Status;

public class TestProcessor implements Processor{
  
  private TestProcessRepository db = new TestProcessRepository();
  private ProcessorConfiguration configuration = new  ProcessorConfigurationImpl();
  
  public TestProcessRepository getProcessRepository(){
    return db;
  }
  
  public ProcessorConfiguration getConfiguration() {
    return configuration;
  }
  
  public ProgressQueue exec(Arg distName, Arg version,
      String profile, Arg processName, int instances) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  public ProgressQueue exec(String execConfigName) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }  

  public ProgressQueue exec(Arg distName, Arg version,
      String profile, int instances) {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }

  public Process getProcess(String corusPid) throws ProcessNotFoundException {
    return db.getProcess(corusPid);
  }
  
  public List<Process> getProcesses() {
    return db.getProcesses();
  }
  
  public List<Process> getProcesses(Arg distName) {
    return db.getProcesses(distName);
  }
  
  public List<Process> getProcesses(Arg distName, Arg version) {
    return db.getProcesses(distName, version);
  }
  
  public List<Process> getProcesses(Arg distName, Arg version,
      String profile) {
    return db.getProcesses(distName, version, profile);
  }
  
  public List<Process> getProcesses(Arg distName, Arg version,
      String profile, Arg processName) {
    return db.getProcesses(distName, version, profile, processName);
  }
  
  public List<Process> getProcessesWithPorts() {
    return new ArrayList<Process>();
  }
  
  public List<Status> getStatus() {
    return new ArrayList<Status>();
  }
  
  public List<Status> getStatus(Arg distName) {
    return getStatus();
  }
  
  public List<Status> getStatus(Arg distName, Arg version) {
    return getStatus();
  }
  
  public List<Status> getStatus(Arg distName, Arg version,
      String profile) {
    return getStatus();
  }
  
  public List<Status> getStatus(Arg distName, Arg version,
      String profile, Arg processName) {
    return getStatus();
  }
  
  public ProcStatus getStatusFor(String corusPid) throws ProcessNotFoundException {
    return new ProcStatus(getProcess(corusPid));
  }
  
  public void kill(Arg distName, Arg version, String profile,
      boolean suspend) {
  }
  
  public void kill(Arg distName, Arg version, String profile,
      Arg processName, boolean suspend) {
  }
  
  public void kill(String corusPid, boolean suspend) throws ProcessNotFoundException{
  }
  
  public void restart(String pid) throws ProcessNotFoundException{
  }
  
  public void restartByAdmin(String pid) throws ProcessNotFoundException{
  }
  
  public void restart(Arg distName, Arg version, String profile) {
  }
  
  public void restart(Arg distName, Arg version, String profile, Arg processName) {
  }
  
  public ProgressQueue resume() {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  @Override
  public void addExecConfig(ExecConfig conf) {
  }
  
  
  public List<ExecConfig> getExecConfigs() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void removeExecConfig(Arg name) {
    // TODO Auto-generated method stub
    
  }
  
  public String getRoleName() {
    return Processor.ROLE;
  }

  public ProcessRepository getProcessDB(){
    return db;
  }
}
