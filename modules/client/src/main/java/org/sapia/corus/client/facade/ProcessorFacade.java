package org.sapia.corus.client.facade;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;

/**
 * This interface specifies a facade to the Corus {@link Processor}
 * 
 * @author yduchesne
 * 
 */
public interface ProcessorFacade {

  /**
   * Return the process whose identifier is given..
   * 
   * @param pid
   *          a Corus process identifier.
   * @return the {@link Process} instance whose ID is given.
   */
  public Process getProcess(String pid) throws ProcessNotFoundException;

  /**
   * Returns the processes corresponding to the given criteria, per Corus
   * server.
   * 
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return a {@link Results} containing the matching {@link Process}
   *         instances.
   */
  public Results<List<Process>> getProcesses(ProcessCriteria criteria, ClusterInfo cluster);
  
  
  /**
   * Deploys the execution configuration whose file is given.
   * 
   * @param file
   *          the the {@link File} of the execution configuration to deploy.
   * @param cluster
   */
  public void deployExecConfig(File file, ClusterInfo cluster) throws IOException, Exception;

  /**
   * Undeploys the execution configurations matching the given criteria.
   * 
   * @param criteria the {@link ExecConfigCriteria} to use to match
   *          desired configs for undeployment.
   * @param cluster
   */
  public void undeployExecConfig(ExecConfigCriteria criteria, ClusterInfo cluster);

  /**
   * Returns the {@link ExecConfig}s in the system.
   * 
   * @param criteria the {@link ExecConfigCriteria} to use to match
   *          the desired configs.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return the {@link Results} containing the {@link ExecConfig}s in the
   *         system.
   */
  public Results<List<ExecConfig>> getExecConfigs(ExecConfigCriteria criteria, ClusterInfo cluster);


  /**
   * Starts process(es) corresponding to an existing execution configuration.
   * 
   * @param criteria the {@link ExecConfigCriteria} to use to match
   *          the desired configs.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue execConfig(ExecConfigCriteria criteria, ClusterInfo cluster);
  
  /**
   * @param criteria the {@link ExecConfigCriteria} to use to match
   *          the desired configs.
   * @param enabled
   *          If <code>true</code> enables the corresponding exec configs. Disables 
   *          them otherwise.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void setExecConfigEnabled(ExecConfigCriteria criteria, boolean enabled, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param instances
   *          the number of process(es) to start.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   */
  public ProgressQueue exec(ProcessCriteria criteria, int instances, ClusterInfo cluster) throws TooManyProcessInstanceException;

  /**
   * Restarts the process with the given Corus process ID.
   * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>false</code>.
   * 
   * @param pid
   *          a Corus process identifier.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void restart(String pid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * Restarts the process(es) corresponding to the passed in parameters.
   * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>false</code>.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param prefs
   *          a {@link KillPreferences} instance.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public ProgressQueue restart(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster);

  /**
   * Resumes the suspended process(es) corresponding to the passed in
   * parameters.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public ProgressQueue resume(ProcessCriteria criteria, ClusterInfo cluster);

  /**
   * Kills the process with the given Corus process ID.
   * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>false</code>.
   * 
   * @param pid
   *          a Corus process identifier.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void kill(String pid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * Kills the process(es) corresponding to the passed in parameters.
   * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>false</code>.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param prefs
   *          a {@link KillPreferences} instance.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void kill(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster);

  /**
   * Suspends the process(es) corresponding to the passed in parameters.
    * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>true</code>.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param prefs
   *          a {@link KillPreferences} instance.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void suspend(ProcessCriteria criteria, KillPreferences prefs, ClusterInfo cluster);

  /**
   * Suspends the process with the given Corus process ID.
   * <p>
   * Note: the <code>suspend</code> flag of the given kill preferences is forced to <code>true</code>.
   * 
   * @param pid
   *          a Corus process identifier.
   * @param prefs
   *          a {@link KillPreferences} instance.
   */
  public void suspend(String pid, KillPreferences prefs) throws ProcessNotFoundException;

  /**
   * Return the status of the process whose identifier is given..
   * 
   * @param pid
   *          a process identifier.
   * @return a {@link ProcStatus} instance.
   */
  public ProcStatus getStatusFor(String pid) throws ProcessNotFoundException;

  /**
   * Returns the status of the processes corresponding to the given criteria.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public Results<List<ProcStatus>> getStatus(ProcessCriteria criteria, ClusterInfo cluster);
  
  /**
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void clean(ClusterInfo cluster);

}
