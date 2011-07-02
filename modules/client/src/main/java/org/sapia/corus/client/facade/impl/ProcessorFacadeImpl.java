package org.sapia.corus.client.facade.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

public class ProcessorFacadeImpl extends FacadeHelper<Processor> implements ProcessorFacade{

  
  public ProcessorFacadeImpl(CorusConnectionContext context) {
    super(context, Processor.class);
  }

  @Override
  public synchronized void deployExecConfig(String fileName, ClusterInfo cluster) throws IOException, Exception{
    FileInputStream fis = new FileInputStream(fileName); 
    ExecConfig conf = ExecConfig.newInstance(fis);
    proxy.addExecConfig(conf);
    try{
      invoker.invoke(void.class, cluster);
    }catch(IOException e){
      throw e;
    }catch(Exception e){
      throw e;
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void undeployExecConfig(String fileName, ClusterInfo cluster){
    proxy.removeExecConfig(ArgFactory.parse(fileName));
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public synchronized Results<List<ExecConfig>> getExecConfigs(ClusterInfo cluster){
    Results<List<ExecConfig>>  results = new Results<List<ExecConfig>>();
    proxy.getExecConfigs();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized ProgressQueue exec(String configName, ClusterInfo cluster) {
    proxy.exec(configName);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public synchronized ProgressQueue exec(String distName, String version, String profile,
      int instances, ClusterInfo cluster) throws TooManyProcessInstanceException{
    proxy.exec(ArgFactory.parse(distName), ArgFactory.parse(version), profile, instances);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public synchronized ProgressQueue exec(String distName, String version, String profile,
      String processName, int instances, ClusterInfo cluster) throws TooManyProcessInstanceException{
    proxy.exec(ArgFactory.parse(distName), 
        ArgFactory.parse(version), 
        profile, 
        ArgFactory.parse(processName), 
        instances);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public synchronized Process getProcess(String pid) throws ProcessNotFoundException {
    return context.lookup(Processor.class).getProcess(pid);
  }
  
  @Override
  public synchronized Results<List<Process>> getProcesses(ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<Process>> getProcesses(String distName, ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses(ArgFactory.parse(distName));
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<Process>> getProcesses(String distName, String version,
      ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses(ArgFactory.parse(distName), ArgFactory.parse(version));
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<Process>> getProcesses(String distName, String version,
      String profile, ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses(ArgFactory.parse(distName), ArgFactory.parse(version), profile);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public synchronized Results<List<Process>> getProcesses(String distName, String version,
      String profile, String processName, ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses(ArgFactory.parse(distName), ArgFactory.parse(version), profile, ArgFactory.parse(processName));
    invoker.invokeLenient(results, cluster);
    return results;
  }
   
  @Override
  public synchronized void kill(String distName, String version, String profile,
      ClusterInfo cluster) {
    proxy.kill(ArgFactory.parse(distName), ArgFactory.parse(version), profile, false);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public synchronized void kill(String distName, String version, String profile,
      String processName, ClusterInfo cluster) {
    proxy.kill(ArgFactory.parse(distName), ArgFactory.parse(version), profile, false);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public synchronized void kill(String pid) throws ProcessNotFoundException {
    context.lookup(Processor.class).kill(pid, false);
  }
  
  @Override
  public synchronized ProgressQueue restart(ClusterInfo cluster) {
    proxy.resume();
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public synchronized void restart(String pid) throws ProcessNotFoundException {
    context.lookup(Processor.class).restart(pid);
  }
  
  @Override
  public synchronized void suspend(String distName, String version, String profile,
      ClusterInfo cluster) {
    proxy.kill(ArgFactory.parse(distName), ArgFactory.parse(version), profile, true);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public synchronized void suspend(String distName, String version, String profile,
      String processName, ClusterInfo cluster) {
    proxy.kill(ArgFactory.parse(distName), 
        ArgFactory.parse(version), 
        profile,
        ArgFactory.parse(processName),
        true);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public synchronized void suspend(String pid) throws ProcessNotFoundException {
    context.lookup(Processor.class).kill(pid, true);
  }
  
  @Override
  public synchronized Results<List<ProcStatus>> getStatus(ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<ProcStatus>> getStatus(String distName, ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus(ArgFactory.parse(distName));
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<ProcStatus>> getStatus(String distName, String version,
      ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus(ArgFactory.parse(distName), ArgFactory.parse(version));
    invoker.invokeLenient(results, cluster);
    return results;
  }
 
  @Override
  public synchronized Results<List<ProcStatus>> getStatus(String distName, String version,
      String profile, ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus(ArgFactory.parse(distName), ArgFactory.parse(version), profile);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<ProcStatus>> getStatus(String distName, String version,
      String profile, String processName, ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus(
        ArgFactory.parse(distName), 
        ArgFactory.parse(version), 
        profile,
        ArgFactory.parse(processName));
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized ProcStatus getStatusFor(String pid) throws ProcessNotFoundException {
    return context.lookup(Processor.class).getStatusFor(pid);
  }
  
}
