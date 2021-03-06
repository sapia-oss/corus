package org.sapia.corus.client.facade.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;

public class ProcessorFacadeImpl extends FacadeHelper<Processor> implements ProcessorFacade {

  public ProcessorFacadeImpl(CorusConnectionContext context) {
    super(context, Processor.class);
  }

  @Override
  public void deployExecConfig(File file, ClusterInfo cluster) throws IOException, Exception {
    FileInputStream fis = new FileInputStream(file);
    ExecConfig conf = ExecConfig.newInstance(fis);
    proxy.addExecConfig(conf);
    try {
      invoker.invoke(void.class, cluster);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void undeployExecConfig(ExecConfigCriteria criteria, ClusterInfo cluster) {
    proxy.removeExecConfig(criteria);
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public Results<List<ExecConfig>> getExecConfigs(ExecConfigCriteria criteria, ClusterInfo cluster) {
    Results<List<ExecConfig>> results = new Results<List<ExecConfig>>();
    proxy.getExecConfigs(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public ProgressQueue execConfig(ExecConfigCriteria criteria, ClusterInfo cluster) {
    proxy.execConfig(criteria);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public void setExecConfigEnabled(ExecConfigCriteria criteria, boolean enabled,
      ClusterInfo cluster) {
    proxy.setExecConfigEnabled(criteria, enabled);
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public ProgressQueue exec(ProcessCriteria criteria, int instances, ClusterInfo cluster) throws TooManyProcessInstanceException {
    proxy.exec(criteria, instances);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

  @Override
  public Process getProcess(String pid) throws ProcessNotFoundException {
    return context.lookup(Processor.class).getProcess(pid);
  }

  @Override
  public Results<List<Process>> getProcesses(ProcessCriteria criteria, ClusterInfo cluster) {
    Results<List<Process>> results = new Results<List<Process>>();
    proxy.getProcesses(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public void kill(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster) {
    proxy.kill(criteria, prefs.setSuspend(false));
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void kill(String pid, KillPreferences prefs) throws ProcessNotFoundException {
    context.lookup(Processor.class).kill(pid, prefs.setSuspend(false));
  }

  @Override
  public void restart(String pid, KillPreferences prefs) throws ProcessNotFoundException {
    context.lookup(Processor.class).restart(pid, prefs.setSuspend(false));
  }

  @Override
  public ProgressQueue restart(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster) {
    proxy.restart(criteria, prefs.setSuspend(false));
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

  @Override
  public ProgressQueue resume(ProcessCriteria criteria, ClusterInfo cluster) {
    proxy.resume(criteria);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

  @Override
  public void suspend(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster) {
    proxy.kill(criteria, prefs.setSuspend(true));
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void suspend(String pid, KillPreferences prefs) throws ProcessNotFoundException {
    context.lookup(Processor.class).kill(pid, prefs.setSuspend(true));
  }

  @Override
  public Results<List<ProcStatus>> getStatus(ProcessCriteria criteria, ClusterInfo cluster) {
    Results<List<ProcStatus>> results = new Results<List<ProcStatus>>();
    proxy.getStatus(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public ProcStatus getStatusFor(String pid) throws ProcessNotFoundException {
    return context.lookup(Processor.class).getStatusFor(pid);
  }
  
  @Override
  public void clean(ClusterInfo cluster) {
    proxy.clean();
    invoker.invokeLenient(void.class, cluster);  
  }
  
  @Override
  public void archiveExecConfigs(RevId revId, ClusterInfo cluster) {
    proxy.archiveExecConfigs(revId);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void unarchiveExecConfigs(RevId revId, ClusterInfo cluster) {
    proxy.unarchiveExecConfigs(revId);
    invoker.invokeLenient(void.class, cluster);
  }

}
