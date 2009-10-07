package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.processor.ExecConfig;
import org.sapia.corus.admin.services.processor.ProcStatus;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.admin.services.processor.ProcessorConfiguration;
import org.sapia.corus.admin.services.processor.ProcessorConfigurationImpl;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.interop.Status;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;

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

  public Process getProcess(String corusPid) throws LogicException {
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
  
  public ProcStatus getStatusFor(String corusPid) throws LogicException {
    return new ProcStatus(getProcess(corusPid));
  }
  
  public void kill(Arg distName, Arg version, String profile,
      boolean suspend) throws CorusException {
  }
  
  public void kill(Arg distName, Arg version, String profile,
      Arg processName, boolean suspend) throws CorusException {
  }
  
  public void kill(String corusPid, boolean suspend) throws CorusException {
  }
  
  public void restart(String pid) throws CorusException {
  }
  
  public void restartByAdmin(String pid) throws CorusException {
  }
  
  public ProgressQueue resume() {
    ProgressQueue q = new ProgressQueueImpl();
    return q;
  }
  
  public void addExecConfig(ExecConfig conf) throws LogicException {
    // TODO Auto-generated method stub
    
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
