package org.sapia.corus.client.services.processor;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.interop.Status;

/**
 * This interface specifies remote process administration behavior.
 * 
 * @author Yanick Duchesne
 */

public interface Processor extends java.rmi.Remote, Module, Dumpable {
  String ROLE = Processor.class.getName();

  /**
   * @return this instance's state.
   */
  public Reference<ModuleState> getState();
  
  /**
   * Starts process(es) corresponding to the passed in parameters.
   * 
   * @param criteria
   *          the {@link ProcessCriteria} indicating which process should be
   *          executed.
   * @param instances
   *          the number of process(es) to start.
   * 
   * @return a {@link ProgressQueue}
   */
  public ProgressQueue exec(ProcessCriteria criteria, int instances) throws TooManyProcessInstanceException;

  /**
   * Resumes all suspended processes.
   * 
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue resume();

  /**
   * Shuts down and restarts the process with the given ID.
   * 
   * @param pid
   *          a Corus pid.
   * @param prefs
   *          a {@link KillPreferences} instance. 
   */
  public void restartByAdmin(String pid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * Restarts the process with the given ID.
   * 
   * @see Processor#restart(String)
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void restart(String pid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * Restarts the process(es) corresponding to the passed in parameters.
   * 
   * @param criteria
   *          the {@link ProcessCriteria} indicating which process should be
   *          restarted.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void restart(ProcessCriteria criteria, KillPreferences prefs);

  /**
   * Kill the process(es) corresponding to the passed in parameters.
   * 
   * @param criteria
   *          the {@link ProcessCriteria} indicating which process should be
   *          restarted.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void kill(ProcessCriteria criteria, KillPreferences prefs);

  /**
   * Kills the process with the given identifier.
   * 
   * @param corusPid
   *          a process identifier.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void kill(String corusPid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue resume(ProcessCriteria criteria);

  /**
   * Performs cleanup following process shutdown confirmation.
   * 
   * @param corusPid
   *          a process identifier.
   */
  public void confirmShutdown(String corusPid) throws ProcessNotFoundException;

  /**
   * Return the process whose identifier is given.
   * 
   * @param corusPid
   *          a process identifier.
   * @return a {@link Process} instance.
   */
  public Process getProcess(String corusPid) throws ProcessNotFoundException;

  /**
   * Returns the processes that match the given criteria.
   */
  public List<Process> getProcesses(ProcessCriteria criteria);

  /**
   * Return the status of the process whose identifier is given..
   * 
   * @param corusPid
   *          a process identifier.
   * @return a <code>Status</code> instance.
   */
  public ProcStatus getStatusFor(String corusPid) throws ProcessNotFoundException;

  /**
   * Returns the status for the processes that match the given criteria.
   * 
   * @param criteria
   *          the {@link ProcessCriteria} indicating which process should be
   *          restarted.
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus(ProcessCriteria criteria);

  /**
   * Returns all processes that have acquired one or more ports.
   * 
   * @return a {@link List} of {@link Process} instances.
   */
  public List<Process> getProcessesWithPorts();

  /**
   * @param conf
   *          adds an {@link ExecConfig} to this instance.
   */
  public void addExecConfig(ExecConfig conf);

  /**
   * @param criteria the {@link ExecConfigCriteria} to use for selecting
   *          desired configs.
   * @return the list of {@link ExecConfig}s that are contained in this
   *          instance.
   */
  public List<ExecConfig> getExecConfigs(ExecConfigCriteria criteria);
  
  /**
   * @param criteria the {@link ExecConfigCriteria} to use for selecting
   *          desired configs.
   * @return a {@link ProgressQueue}
   */
  public ProgressQueue execConfig(ExecConfigCriteria criteria);
  
  /**
   * @param ethe {@link ExecConfigCriteria} to use for selecting
   *          desired configs.
   * @param execConfigEnable if <code>true</code>, enables the selected configs.
   */
  public void setExecConfigEnabled(ExecConfigCriteria criteria, boolean enabled);
  
  /**
   * Archives all currently stored exec configs.
   * 
   * @param revId the revision ID to use.
   */
  public void archiveExecConfigs(RevId revId);
  
  /**
   * Unarchives exec configs that have been archived using the given revision ID. 
   * 
   * @param revId the revision ID to use.
   */
  public void unarchiveExecConfigs(RevId revId);

  /**
   * @param the {@link ExecConfigCriteria} to use for selecting
   *          desired configs.
   */          
  public void removeExecConfig(ExecConfigCriteria criteria);
  
  /**
   * Cleans process info.
   */
  public void clean();

  /**
   * @return this instance's {@link ProcessorConfiguration}
   */
  public ProcessorConfiguration getConfiguration();
  
}
